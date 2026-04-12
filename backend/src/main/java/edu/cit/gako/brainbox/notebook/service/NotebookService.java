package edu.cit.gako.brainbox.notebook.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.gako.brainbox.auth.service.UserService;
import edu.cit.gako.brainbox.exception.ForbiddenException;
import edu.cit.gako.brainbox.exception.NotebookVersionConflictException;
import edu.cit.gako.brainbox.notebook.builder.NotebookBuilder;
import edu.cit.gako.brainbox.notebook.dto.request.NotebookRequest;
import edu.cit.gako.brainbox.notebook.dto.request.OfflineNotebookBundleRequest;
import edu.cit.gako.brainbox.notebook.dto.response.FlashcardResponse;
import edu.cit.gako.brainbox.notebook.dto.response.NotebookFullResponse;
import edu.cit.gako.brainbox.notebook.dto.response.NotebookOverviewResponse;
import edu.cit.gako.brainbox.notebook.dto.response.OfflineNotebookBundleItem;
import edu.cit.gako.brainbox.notebook.dto.response.OfflineNotebookBundleResponse;
import edu.cit.gako.brainbox.notebook.dto.response.PlaylistResponse;
import edu.cit.gako.brainbox.notebook.dto.response.QuizResponse;
import edu.cit.gako.brainbox.notebook.entity.Category;
import edu.cit.gako.brainbox.notebook.entity.Flashcard;
import edu.cit.gako.brainbox.notebook.entity.NotebookMutationRecord;
import edu.cit.gako.brainbox.notebook.entity.NotebookMutationType;
import edu.cit.gako.brainbox.notebook.entity.Notebook;
import edu.cit.gako.brainbox.notebook.entity.Playlist;
import edu.cit.gako.brainbox.notebook.entity.Quiz;
import edu.cit.gako.brainbox.notebook.repository.CategoryRepository;
import edu.cit.gako.brainbox.notebook.repository.FlashcardAttemptRepository;
import edu.cit.gako.brainbox.notebook.repository.FlashcardRepository;
import edu.cit.gako.brainbox.notebook.repository.NotebookMutationRecordRepository;
import edu.cit.gako.brainbox.notebook.repository.NotebookRepository;
import edu.cit.gako.brainbox.notebook.repository.NotebookVersionRepository;
import edu.cit.gako.brainbox.notebook.repository.PlaylistRepository;
import edu.cit.gako.brainbox.notebook.repository.QuizAttemptRepository;
import edu.cit.gako.brainbox.notebook.repository.QuizRepository;
import edu.cit.gako.brainbox.notebook.event.NotebookContentSavedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotebookService {

    private final NotebookRepository notebookRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotebookVersionRepository notebookVersionRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardAttemptRepository flashcardAttemptRepository;
    private final PlaylistRepository playlistRepository;
    private final NotebookMutationRecordRepository notebookMutationRecordRepository;
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

    @Transactional(readOnly = true)
    public List<NotebookFullResponse> getAllFullNotebooks() {
        return notebookRepository.findAll().stream()
                .map(this::mapToFullResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotebookFullResponse> getFullNotebooksByUser(Long userId) {
        return notebookRepository.findByUserId(userId).stream()
                .map(this::mapToFullResponse)
                .toList();
    }

    public Notebook getNotebookById(Long notebookId) {
        return notebookRepository.findById(notebookId)
                .orElseThrow(() -> new NoSuchElementException("Notebook not found"));
    }

    @Transactional(readOnly = true)
    public Notebook getNotebookByUuid(String uuid) {
        return notebookRepository.findByUuid(uuid)
                .orElseThrow(() -> new NoSuchElementException("Notebook not found"));
    }

    @Transactional(readOnly = true)
    public Notebook getNotebookByUuidAndUserId(String uuid, Long userId) {
        return notebookRepository.findByUuidAndUserId(uuid, userId)
                .orElseThrow(() -> {
                    if (notebookRepository.existsByUuid(uuid)) {
                        return new ForbiddenException("You do not have access to this resource");
                    }
                    return new NoSuchElementException("Notebook not found");
                });
    }

    @Transactional(readOnly = true)
    public NotebookFullResponse getFullNotebookResponseByUuid(String uuid, Long userId) {
        return mapToFullResponse(getNotebookByUuidAndUserId(uuid, userId));
    }

    @Transactional(readOnly = true)
    public List<NotebookOverviewResponse> getNotebookOverviewsByUser(Long userId) {
        return notebookRepository.findByUserId(userId).stream()
                .map(this::mapToOverviewResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotebookOverviewResponse> getRecentlyEditedNotebooksByUser(Long userId) {
        return notebookRepository.findTop6ByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(this::mapToOverviewResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotebookOverviewResponse> getRecentlyReviewedNotebooksByUser(Long userId) {
        return notebookRepository.findTop3ByUserIdAndLastReviewedAtNotNullOrderByLastReviewedAtDesc(userId).stream()
                .map(this::mapToOverviewResponse)
                .toList();
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
                () -> {
                    notebookRepository.updateLastReviewedAt(notebook.getUuid(), userId, Instant.now());
                }
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
    public OfflineNotebookBundleResponse getOfflineNotebookBundle(OfflineNotebookBundleRequest request, Long userId) {
        List<String> requestedUuids = normalizeRequestedUuids(request != null ? request.getNotebookUuids() : null);
        List<Notebook> notebooks = requestedUuids.isEmpty()
                ? List.of()
                : notebookRepository.findByUuidInAndUserId(requestedUuids, userId);

        Map<String, Notebook> notebookByUuid = notebooks.stream()
                .collect(LinkedHashMap::new, (map, notebook) -> map.put(notebook.getUuid(), notebook), Map::putAll);

        List<Quiz> quizzes = quizRepository.findByUserId(userId);
        Map<Long, List<Quiz>> quizzesByNotebookId = quizzes.stream()
                .filter(quiz -> quiz.getNotebook() != null && quiz.getNotebook().getId() != null)
                .collect(java.util.stream.Collectors.groupingBy(quiz -> quiz.getNotebook().getId()));

        List<Flashcard> flashcards = flashcardRepository.findByUserId(userId);
        Map<Long, List<Flashcard>> flashcardsByNotebookId = flashcards.stream()
                .filter(flashcard -> flashcard.getNotebook() != null && flashcard.getNotebook().getId() != null)
                .collect(java.util.stream.Collectors.groupingBy(flashcard -> flashcard.getNotebook().getId()));

        List<Playlist> playlists = playlistRepository.findByUserId(userId);
        Map<Long, List<Playlist>> playlistsByNotebookId = new LinkedHashMap<>();
        for (Playlist playlist : playlists) {
            for (Notebook queuedNotebook : playlist.getQueue()) {
                if (queuedNotebook.getId() == null) {
                    continue;
                }
                playlistsByNotebookId.computeIfAbsent(queuedNotebook.getId(), key -> new java.util.ArrayList<>())
                        .add(playlist);
            }
        }

        List<OfflineNotebookBundleItem> items = requestedUuids.stream()
                .map(uuid -> notebookByUuid.get(uuid))
                .filter(Objects::nonNull)
                .map(notebook -> {
                    OfflineNotebookBundleItem item = new OfflineNotebookBundleItem();
                    item.setNotebook(mapToFullResponse(notebook));
                    item.setQuizzes(quizzesByNotebookId.getOrDefault(notebook.getId(), List.of()).stream()
                            .map(this::mapToQuizResponse)
                            .toList());
                    item.setFlashcards(flashcardsByNotebookId.getOrDefault(notebook.getId(), List.of()).stream()
                            .map(this::mapToFlashcardResponse)
                            .toList());
                    item.setPlaylists(playlistsByNotebookId.getOrDefault(notebook.getId(), List.of()).stream()
                            .distinct()
                            .sorted(Comparator.comparing(Playlist::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                            .map(this::mapToPlaylistResponse)
                            .toList());
                    return item;
                })
                .toList();

        OfflineNotebookBundleResponse response = new OfflineNotebookBundleResponse();
        response.setNotebooks(items);
        response.setMissingUuids(requestedUuids.stream()
                .filter(uuid -> !notebookByUuid.containsKey(uuid))
                .toList());
        return response;
    }

    @Transactional
    public NotebookFullResponse touchNotebook(Notebook notebook) {
        notebook.setUpdatedAt(Instant.now());
        return mapToFullResponse(notebookRepository.saveAndFlush(notebook));
    }

    @Transactional
    public void deleteNotebook(Notebook notebook) {
        detachNotebookRelations(notebook);
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

    private List<String> normalizeRequestedUuids(Collection<String> notebookUuids) {
        if (notebookUuids == null) {
            return List.of();
        }

        return notebookUuids.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(uuid -> !uuid.isBlank())
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                        List::copyOf));
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

    private QuizResponse mapToQuizResponse(Quiz quiz) {
        QuizResponse response = new QuizResponse();
        response.setUuid(quiz.getUuid());
        response.setTitle(quiz.getTitle());
        response.setDescription(quiz.getDescription());
        response.setDifficulty(quiz.getDifficulty());
        response.setCreatedAt(quiz.getCreatedAt());
        response.setUpdatedAt(quiz.getUpdatedAt());

        if (quiz.getNotebook() != null) {
            response.setNotebookUuid(quiz.getNotebook().getUuid());
            response.setNotebookTitle(quiz.getNotebook().getTitle());
        }

        List<edu.cit.gako.brainbox.notebook.dto.response.QuizQuestionResponse> questions = quiz.getQuestions().stream().map(q -> {
            edu.cit.gako.brainbox.notebook.dto.response.QuizQuestionResponse qr = new edu.cit.gako.brainbox.notebook.dto.response.QuizQuestionResponse();
            qr.setType(q.getType());
            qr.setText(q.getText());
            qr.setOptions(q.getOptions());
            qr.setCorrectIndex(q.getCorrectIndex());
            return qr;
        }).toList();

        response.setQuestions(questions);
        response.setQuestionCount(questions.size());
        response.setEstimatedTime(Math.max(1, questions.size() * 2) + " min");
        response.setAttempts(quizAttemptRepository.countByQuizId(quiz.getId()));
        response.setBestScore(quizAttemptRepository.findBestScoreByQuizId(quiz.getId()).orElse(null));

        return response;
    }

    private FlashcardResponse mapToFlashcardResponse(Flashcard flashcard) {
        FlashcardResponse response = new FlashcardResponse();
        response.setUuid(flashcard.getUuid());
        response.setTitle(flashcard.getTitle());
        response.setDescription(flashcard.getDescription());
        response.setCreatedAt(flashcard.getCreatedAt());
        response.setUpdatedAt(flashcard.getUpdatedAt());

        if (flashcard.getNotebook() != null) {
            response.setNotebookUuid(flashcard.getNotebook().getUuid());
            response.setNotebookTitle(flashcard.getNotebook().getTitle());
        }

        List<edu.cit.gako.brainbox.notebook.dto.response.FlashcardCardResponse> cards = flashcard.getCards().stream().map(c -> {
            edu.cit.gako.brainbox.notebook.dto.response.FlashcardCardResponse cr = new edu.cit.gako.brainbox.notebook.dto.response.FlashcardCardResponse();
            cr.setFront(c.getFront());
            cr.setBack(c.getBack());
            return cr;
        }).toList();

        response.setCards(cards);
        response.setCardCount(cards.size());
        response.setAttempts(flashcardAttemptRepository.countByFlashcardId(flashcard.getId()));
        response.setBestMastery(flashcardAttemptRepository.findBestMasteryByFlashcardId(flashcard.getId()).orElse(null));
        return response;
    }

    private PlaylistResponse mapToPlaylistResponse(Playlist playlist) {
        PlaylistResponse response = new PlaylistResponse();
        response.setUuid(playlist.getUuid());
        response.setTitle(playlist.getTitle());
        response.setCurrentIndex(playlist.getCurrentIndex());
        response.setCreatedAt(playlist.getCreatedAt());
        response.setUpdatedAt(playlist.getUpdatedAt());
        response.setQueue(playlist.getQueue().stream().map(this::mapToOverviewResponse).toList());
        return response;
    }

    private void detachNotebookRelations(Notebook notebook) {
        Long notebookId = notebook.getId();

        List<Playlist> playlists = playlistRepository.findDistinctByQueueId(notebookId);
        playlists.forEach((playlist) -> {
            playlist.getQueue().removeIf((queuedNotebook) -> notebookId.equals(queuedNotebook.getId()));
            normalizePlaylistIndex(playlist);
        });
        if (!playlists.isEmpty()) {
            playlistRepository.saveAll(playlists);
        }

        List<Quiz> quizzes = quizRepository.findByNotebookId(notebookId);
        quizzes.forEach((quiz) -> quiz.setNotebook(null));
        if (!quizzes.isEmpty()) {
            quizRepository.saveAll(quizzes);
        }

        List<Flashcard> flashcards = flashcardRepository.findByNotebookId(notebookId);
        flashcards.forEach((flashcard) -> flashcard.setNotebook(null));
        if (!flashcards.isEmpty()) {
            flashcardRepository.saveAll(flashcards);
        }

        notebookVersionRepository.deleteByNotebookId(notebookId);
    }

    private void normalizePlaylistIndex(Playlist playlist) {
        if (playlist.getQueue().isEmpty()) {
            playlist.setCurrentIndex(0);
            return;
        }

        if (playlist.getCurrentIndex() >= playlist.getQueue().size()) {
            playlist.setCurrentIndex(playlist.getQueue().size() - 1);
        }
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
