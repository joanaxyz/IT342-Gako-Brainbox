package edu.cit.gako.brainbox.notebook.service;

import edu.cit.gako.brainbox.auth.service.UserService;
import edu.cit.gako.brainbox.exception.ForbiddenException;
import edu.cit.gako.brainbox.notebook.dto.request.NotebookRequest;
import edu.cit.gako.brainbox.notebook.dto.response.NotebookFullResponse;
import edu.cit.gako.brainbox.notebook.dto.response.NotebookOverviewResponse;
import edu.cit.gako.brainbox.notebook.entity.Category;
import edu.cit.gako.brainbox.notebook.entity.Flashcard;
import edu.cit.gako.brainbox.notebook.entity.Notebook;
import edu.cit.gako.brainbox.notebook.entity.Playlist;
import edu.cit.gako.brainbox.notebook.entity.Quiz;
import edu.cit.gako.brainbox.notebook.repository.CategoryRepository;
import edu.cit.gako.brainbox.notebook.repository.FlashcardRepository;
import edu.cit.gako.brainbox.notebook.repository.NotebookRepository;
import edu.cit.gako.brainbox.notebook.repository.NotebookVersionRepository;
import edu.cit.gako.brainbox.notebook.repository.PlaylistRepository;
import edu.cit.gako.brainbox.notebook.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotebookService {

    private final NotebookRepository notebookRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final NotebookVersionSnapshotService notebookVersionSnapshotService;
    private final NotebookVersionRepository notebookVersionRepository;
    private final QuizRepository quizRepository;
    private final FlashcardRepository flashcardRepository;
    private final PlaylistRepository playlistRepository;

    @Transactional
    public NotebookFullResponse createNotebook(NotebookRequest request, Long userId) {
        Notebook notebook = new Notebook();
        notebook.setTitle(request.getTitle());
        notebook.setContent(request.getContent() != null ? request.getContent() : "");

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NoSuchElementException("Category not found"));
            notebook.setCategory(category);
        }

        notebook.setUser(userService.findById(userId));
        Notebook savedNotebook = notebookRepository.saveAndFlush(notebook);

        notebookVersionSnapshotService.createSnapshot(savedNotebook, savedNotebook.getContent());

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
        Notebook notebook = getNotebookByUuidAndUserId(uuid, userId);

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

        return mapToFullResponse(notebookRepository.saveAndFlush(notebook));
    }

    @Transactional
    public NotebookFullResponse saveContent(String uuid, Long userId, String content) {
        Notebook notebook = getNotebookByUuidAndUserId(uuid, userId);
        String nextContent = content != null ? content : "";

        if (nextContent.equals(notebook.getContent())) {
            return mapToFullResponse(notebook);
        }

        notebook.setContent(nextContent);
        Notebook savedNotebook = notebookRepository.saveAndFlush(notebook);
        notebookVersionSnapshotService.createSnapshot(savedNotebook, savedNotebook.getContent());
        return mapToFullResponse(savedNotebook);
    }

    @Transactional
    public void markNotebookReviewed(String uuid, Long userId) {
        notebookRepository.updateLastReviewedAt(uuid, userId, Instant.now());
    }

    @Transactional
    public void deleteNotebook(String uuid, Long userId) {
        Notebook notebook = getNotebookByUuidAndUserId(uuid, userId);
        deleteNotebook(notebook);
    }

    @Transactional
    public Notebook touchNotebook(Notebook notebook) {
        notebook.setUpdatedAt(Instant.now());
        return notebookRepository.saveAndFlush(notebook);
    }

    @Transactional
    public void deleteNotebook(Notebook notebook) {
        detachNotebookRelations(notebook);
        notebookRepository.delete(notebook);
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
