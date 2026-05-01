package edu.cit.gako.brainbox.modules.notebook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.gako.brainbox.modules.category.entity.Category;
import edu.cit.gako.brainbox.modules.category.repository.CategoryRepository;
import edu.cit.gako.brainbox.modules.notebook.dto.request.NotebookRequest;
import edu.cit.gako.brainbox.modules.notebook.dto.response.NotebookFullResponse;
import edu.cit.gako.brainbox.modules.notebook.dto.response.NotebookOverviewResponse;
import edu.cit.gako.brainbox.modules.notebook.service.NotebookDeletionCoordinator;
import edu.cit.gako.brainbox.modules.notebook.event.NotebookContentSavedEvent;
import edu.cit.gako.brainbox.modules.notebook.entity.Notebook;
import edu.cit.gako.brainbox.modules.notebook.entity.NotebookBuilder;
import edu.cit.gako.brainbox.modules.notebook.entity.NotebookMutationRecord;
import edu.cit.gako.brainbox.modules.notebook.entity.NotebookMutationType;
import edu.cit.gako.brainbox.modules.notebook.repository.NotebookMutationRecordRepository;
import edu.cit.gako.brainbox.modules.notebook.repository.NotebookRepository;
import edu.cit.gako.brainbox.modules.user.service.UserService;
import edu.cit.gako.brainbox.shared.exception.ForbiddenException;
import edu.cit.gako.brainbox.shared.exception.NotebookVersionConflictException;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotebookService {

    private final NotebookRepository notebookRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotebookMutationRecordRepository notebookMutationRecordRepository;
    private final NotebookDeletionCoordinator notebookDeletionCoordinator;
    private final ObjectMapper objectMapper;

    @Transactional
    public NotebookFullResponse createNotebook(NotebookRequest request, Long userId) {
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NoSuchElementException("Category not found"));
        }

        Notebook notebook = new NotebookBuilder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(category)
                .owner(userService.findById(userId))
                .build();

        Notebook savedNotebook = notebookRepository.saveAndFlush(notebook);
        eventPublisher.publishEvent(new NotebookContentSavedEvent(this, savedNotebook, savedNotebook.getContent()));
        return mapToFullResponse(savedNotebook);
    }

    public List<NotebookFullResponse> getAllFullNotebooks() {
        return notebookRepository.findAll().stream()
                .map(this::mapToFullResponse)
                .toList();
    }

    public List<NotebookFullResponse> getFullNotebooksByUser(Long userId) {
        return notebookRepository.findByUserId(userId).stream()
                .map(this::mapToFullResponse)
                .toList();
    }

    public Notebook getNotebookById(Long notebookId) {
        return notebookRepository.findById(notebookId)
                .orElseThrow(() -> new NoSuchElementException("Notebook not found"));
    }

    public Notebook getNotebookByUuid(String uuid) {
        return notebookRepository.findByUuid(uuid)
                .orElseThrow(() -> new NoSuchElementException("Notebook not found"));
    }

    public Notebook getNotebookByUuidAndUserId(String uuid, Long userId) {
        return notebookRepository.findByUuidAndUserId(uuid, userId)
                .orElseThrow(() -> {
                    if (notebookRepository.existsByUuid(uuid)) {
                        return new ForbiddenException("You do not have access to this resource");
                    }
                    return new NoSuchElementException("Notebook not found");
                });
    }

    public NotebookFullResponse getFullNotebookResponseByUuid(String uuid, Long userId) {
        return mapToFullResponse(getNotebookByUuidAndUserId(uuid, userId));
    }

    public NotebookFullResponse getFullNotebookResponseByUuid(String uuid) {
        return mapToFullResponse(getNotebookByUuid(uuid));
    }

    public List<NotebookOverviewResponse> getNotebookOverviewsByUser(Long userId) {
        return notebookRepository.findByUserId(userId).stream()
                .map(this::mapToOverviewResponse)
                .toList();
    }

    public List<NotebookOverviewResponse> getRecentlyEditedNotebooksByUser(Long userId) {
        return notebookRepository.findTop6ByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(this::mapToOverviewResponse)
                .toList();
    }

    public List<NotebookOverviewResponse> getRecentlyReviewedNotebooksByUser(Long userId) {
        return notebookRepository.findTop3ByUserIdAndLastReviewedAtNotNullOrderByLastReviewedAtDesc(userId).stream()
                .map(this::mapToOverviewResponse)
                .toList();
    }

    public List<Notebook> getNotebooksByCategoryIdAndUserId(Long categoryId, Long userId) {
        return notebookRepository.findByCategoryIdAndUserId(categoryId, userId);
    }

    @Transactional
    public void clearCategoryByCategoryIdAndUserId(Long categoryId, Long userId) {
        notebookRepository.clearCategoryByCategoryIdAndUserId(categoryId, userId);
    }

    @Transactional
    public NotebookFullResponse updateNotebook(String uuid, Long userId, NotebookRequest request) {
        Optional<NotebookMutationRecord> appliedMutation = findAppliedMutation(userId, request.getClientMutationId());
        if (appliedMutation.isPresent()) {
            return replayMutationResult(appliedMutation.get(), NotebookMutationType.UPDATE);
        }

        Notebook notebook = getNotebookByUuidAndUserId(uuid, userId);
        return applyNotebookMutationWithResponse(
                notebook,
                userId,
                NotebookMutationType.UPDATE,
                request.getClientMutationId(),
                request.getBaseVersion(),
                () -> {
                    if (request.getTitle() != null) {
                        notebook.setTitle(request.getTitle());
                    }
                    if (request.getCategoryId() != null) {
                        if (request.getCategoryId() == -1) {
                            notebook.setCategory(null);
                        } else {
                            Category category = categoryRepository.findById(request.getCategoryId())
                                    .orElseThrow(() -> new NoSuchElementException("Category not found"));
                            notebook.setCategory(category);
                        }
                    }

                    Notebook savedNotebook = notebookRepository.saveAndFlush(notebook);
                    return mapToFullResponse(savedNotebook);
                }
        );
    }

    @Transactional
    public NotebookFullResponse saveContent(String uuid, Long userId, String content, Long baseVersion, String clientMutationId) {
        Optional<NotebookMutationRecord> appliedMutation = findAppliedMutation(userId, clientMutationId);
        if (appliedMutation.isPresent()) {
            return replayMutationResult(appliedMutation.get(), NotebookMutationType.SAVE_CONTENT);
        }

        Notebook notebook = getNotebookByUuidAndUserId(uuid, userId);
        String nextContent = content != null ? content : "";

        return applyNotebookMutationWithResponse(
                notebook,
                userId,
                NotebookMutationType.SAVE_CONTENT,
                clientMutationId,
                baseVersion,
                () -> {
                    if (nextContent.equals(notebook.getContent())) {
                        return mapToFullResponse(notebook);
                    }

                    notebook.setContent(nextContent);
                    Notebook savedNotebook = notebookRepository.saveAndFlush(notebook);
                    eventPublisher.publishEvent(new NotebookContentSavedEvent(this, savedNotebook, savedNotebook.getContent()));
                    return mapToFullResponse(savedNotebook);
                }
        );
    }

    @Transactional
    public void markNotebookReviewed(String uuid, Long userId, Long baseVersion, String clientMutationId) {
        Optional<NotebookMutationRecord> appliedMutation = findAppliedMutation(userId, clientMutationId);
        if (appliedMutation.isPresent()) {
            return;
        }

        Notebook notebook = getNotebookByUuidAndUserId(uuid, userId);
        applyNotebookMutationWithoutResponse(
                notebook,
                userId,
                NotebookMutationType.MARK_REVIEWED,
                clientMutationId,
                baseVersion,
                () -> notebookRepository.updateLastReviewedAt(notebook.getUuid(), userId, Instant.now())
        );
    }

    @Transactional
    public void deleteNotebook(String uuid, Long userId, Long baseVersion, String clientMutationId) {
        Optional<NotebookMutationRecord> appliedMutation = findAppliedMutation(userId, clientMutationId);
        if (appliedMutation.isPresent()) {
            return;
        }

        Notebook notebook = getNotebookByUuidAndUserId(uuid, userId);
        applyNotebookMutationWithoutResponse(
                notebook,
                userId,
                NotebookMutationType.DELETE,
                clientMutationId,
                baseVersion,
                () -> deleteNotebook(notebook)
        );
    }

    @Transactional
    public void deleteNotebook(String uuid) {
        deleteNotebook(getNotebookByUuid(uuid));
    }

    @Transactional
    public NotebookFullResponse saveContent(String uuid, Long userId, String content) {
        return saveContent(uuid, userId, content, null, null);
    }

    @Transactional
    public void markNotebookReviewed(String uuid, Long userId) {
        markNotebookReviewed(uuid, userId, null, null);
    }

    @Transactional
    public void deleteNotebook(String uuid, Long userId) {
        deleteNotebook(uuid, userId, null, null);
    }

    @Transactional
    public NotebookFullResponse touchNotebook(Notebook notebook) {
        notebook.setUpdatedAt(Instant.now());
        return mapToFullResponse(notebookRepository.saveAndFlush(notebook));
    }

    @Transactional
    public void deleteNotebook(Notebook notebook) {
        notebookDeletionCoordinator.detachNotebookRelations(notebook);
        notebookRepository.delete(notebook);
    }

    private NotebookFullResponse applyNotebookMutationWithResponse(
            Notebook notebook,
            Long userId,
            NotebookMutationType mutationType,
            String clientMutationId,
            Long baseVersion,
            java.util.function.Supplier<NotebookFullResponse> action) {
        Optional<NotebookMutationRecord> appliedMutation = findAppliedMutation(userId, clientMutationId);
        if (appliedMutation.isPresent()) {
            return replayMutationResult(appliedMutation.get(), mutationType);
        }

        assertNotebookVersionMatches(notebook, baseVersion);
        NotebookFullResponse result = action.get();
        recordMutation(userId, notebook.getUuid(), mutationType, clientMutationId, result);
        return result;
    }

    private void applyNotebookMutationWithoutResponse(
            Notebook notebook,
            Long userId,
            NotebookMutationType mutationType,
            String clientMutationId,
            Long baseVersion,
            Runnable action) {
        Optional<NotebookMutationRecord> appliedMutation = findAppliedMutation(userId, clientMutationId);
        if (appliedMutation.isPresent()) {
            return;
        }

        assertNotebookVersionMatches(notebook, baseVersion);
        action.run();
        recordMutation(userId, notebook.getUuid(), mutationType, clientMutationId, null);
    }

    private Optional<NotebookMutationRecord> findAppliedMutation(Long userId, String clientMutationId) {
        if (clientMutationId == null || clientMutationId.isBlank()) {
            return Optional.empty();
        }

        return notebookMutationRecordRepository.findByUserIdAndClientMutationId(userId, clientMutationId);
    }

    private void assertNotebookVersionMatches(Notebook notebook, Long baseVersion) {
        if (baseVersion != null && !baseVersion.equals(notebook.getVersion() != null ? notebook.getVersion() : 0L)) {
            throw new NotebookVersionConflictException(
                    "Notebook changed on the server. Refresh and try again.",
                    mapToFullResponse(notebook));
        }
    }

    private void recordMutation(
            Long userId,
            String notebookUuid,
            NotebookMutationType mutationType,
            String clientMutationId,
            NotebookFullResponse response) {
        if (clientMutationId == null || clientMutationId.isBlank()) {
            return;
        }

        NotebookMutationRecord record = new NotebookMutationRecord();
        record.setUserId(userId);
        record.setNotebookUuid(notebookUuid);
        record.setMutationType(mutationType);
        record.setClientMutationId(clientMutationId);
        if (response != null) {
            record.setResponseJson(serializeResponse(response));
        }
        notebookMutationRecordRepository.save(record);
    }

    private NotebookFullResponse replayMutationResult(NotebookMutationRecord record, NotebookMutationType expectedType) {
        if (record.getMutationType() != expectedType) {
            return record.getResponseJson() != null ? deserializeNotebookResponse(record.getResponseJson()) : null;
        }

        if (record.getResponseJson() == null) {
            return null;
        }

        return deserializeNotebookResponse(record.getResponseJson());
    }

    private String serializeResponse(NotebookFullResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize notebook response", e);
        }
    }

    private NotebookFullResponse deserializeNotebookResponse(String responseJson) {
        try {
            return objectMapper.readValue(responseJson, NotebookFullResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize notebook response", e);
        }
    }

    private NotebookFullResponse mapToFullResponse(Notebook notebook) {
        NotebookFullResponse response = new NotebookFullResponse();
        response.setUuid(notebook.getUuid());
        response.setTitle(notebook.getTitle());
        response.setContent(notebook.getContent() != null ? notebook.getContent() : "");
        response.setWordCount(countWords(notebook.getContent()));
        response.setCreatedAt(notebook.getCreatedAt());
        response.setUpdatedAt(notebook.getUpdatedAt());
        response.setLastReviewedAt(notebook.getLastReviewedAt());
        response.setVersion(notebook.getVersion() != null ? notebook.getVersion() : 0L);
        if (notebook.getCategory() != null) {
            response.setCategoryId(notebook.getCategory().getId());
            response.setCategoryName(notebook.getCategory().getName());
        }
        return response;
    }

    private NotebookOverviewResponse mapToOverviewResponse(Notebook notebook) {
        NotebookOverviewResponse response = new NotebookOverviewResponse();
        response.setUuid(notebook.getUuid());
        response.setTitle(notebook.getTitle());
        response.setWordCount(countWords(notebook.getContent()));
        response.setCreatedAt(notebook.getCreatedAt());
        response.setUpdatedAt(notebook.getUpdatedAt());
        response.setLastReviewedAt(notebook.getLastReviewedAt());
        response.setVersion(notebook.getVersion() != null ? notebook.getVersion() : 0L);
        if (notebook.getCategory() != null) {
            response.setCategoryId(notebook.getCategory().getId());
            response.setCategoryName(notebook.getCategory().getName());
        }
        return response;
    }

    private int countWords(String html) {
        if (html == null || html.isBlank()) {
            return 0;
        }

        String plainText = html
                .replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replaceAll("&[^;]+;", " ")
                .trim();

        if (plainText.isBlank()) {
            return 0;
        }

        return plainText.split("\\s+").length;
    }
}
