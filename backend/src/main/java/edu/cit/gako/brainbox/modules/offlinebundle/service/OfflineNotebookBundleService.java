package edu.cit.gako.brainbox.modules.offlinebundle.service;

import edu.cit.gako.brainbox.modules.flashcard.dto.response.FlashcardCardResponse;
import edu.cit.gako.brainbox.modules.flashcard.dto.response.FlashcardResponse;
import edu.cit.gako.brainbox.modules.flashcard.entity.Flashcard;
import edu.cit.gako.brainbox.modules.flashcard.repository.FlashcardAttemptRepository;
import edu.cit.gako.brainbox.modules.flashcard.repository.FlashcardRepository;
import edu.cit.gako.brainbox.modules.notebook.dto.response.NotebookFullResponse;
import edu.cit.gako.brainbox.modules.notebook.dto.response.NotebookOverviewResponse;
import edu.cit.gako.brainbox.modules.notebook.entity.Notebook;
import edu.cit.gako.brainbox.modules.notebook.repository.NotebookRepository;
import edu.cit.gako.brainbox.modules.offlinebundle.dto.request.OfflineNotebookBundleRequest;
import edu.cit.gako.brainbox.modules.offlinebundle.dto.response.OfflineNotebookBundleItem;
import edu.cit.gako.brainbox.modules.offlinebundle.dto.response.OfflineNotebookBundleResponse;
import edu.cit.gako.brainbox.modules.playlist.dto.response.PlaylistResponse;
import edu.cit.gako.brainbox.modules.playlist.entity.Playlist;
import edu.cit.gako.brainbox.modules.playlist.repository.PlaylistRepository;
import edu.cit.gako.brainbox.modules.quiz.dto.response.QuizQuestionResponse;
import edu.cit.gako.brainbox.modules.quiz.dto.response.QuizResponse;
import edu.cit.gako.brainbox.modules.quiz.entity.Quiz;
import edu.cit.gako.brainbox.modules.quiz.repository.QuizAttemptRepository;
import edu.cit.gako.brainbox.modules.quiz.repository.QuizRepository;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OfflineNotebookBundleService {

    private final NotebookRepository notebookRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardAttemptRepository flashcardAttemptRepository;
    private final PlaylistRepository playlistRepository;

    public OfflineNotebookBundleResponse getNotebookBundle(OfflineNotebookBundleRequest request, Long userId) {
        List<String> requestedUuids = normalizeRequestedUuids(request != null ? request.getNotebookUuids() : null);
        List<Notebook> notebooks = requestedUuids.isEmpty()
                ? List.of()
                : notebookRepository.findByUuidInAndUserId(requestedUuids, userId);

        Map<String, Notebook> notebookByUuid = notebooks.stream()
                .collect(LinkedHashMap::new, (map, notebook) -> map.put(notebook.getUuid(), notebook), Map::putAll);

        List<Quiz> quizzes = quizRepository.findByUserId(userId);
        Map<Long, List<Quiz>> quizzesByNotebookId = quizzes.stream()
                .filter((quiz) -> quiz.getNotebook() != null && quiz.getNotebook().getId() != null)
                .collect(java.util.stream.Collectors.groupingBy((quiz) -> quiz.getNotebook().getId()));

        List<Flashcard> flashcards = flashcardRepository.findByUserId(userId);
        Map<Long, List<Flashcard>> flashcardsByNotebookId = flashcards.stream()
                .filter((flashcard) -> flashcard.getNotebook() != null && flashcard.getNotebook().getId() != null)
                .collect(java.util.stream.Collectors.groupingBy((flashcard) -> flashcard.getNotebook().getId()));

        List<Playlist> playlists = playlistRepository.findByUserId(userId);
        Map<Long, List<Playlist>> playlistsByNotebookId = new LinkedHashMap<>();
        for (Playlist playlist : playlists) {
            for (Notebook queuedNotebook : playlist.getQueue()) {
                if (queuedNotebook.getId() == null) {
                    continue;
                }
                playlistsByNotebookId.computeIfAbsent(queuedNotebook.getId(), (key) -> new java.util.ArrayList<>())
                        .add(playlist);
            }
        }

        List<OfflineNotebookBundleItem> items = requestedUuids.stream()
                .map(notebookByUuid::get)
                .filter(Objects::nonNull)
                .map((notebook) -> {
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
                .filter((uuid) -> !notebookByUuid.containsKey(uuid))
                .toList());
        return response;
    }

    private List<String> normalizeRequestedUuids(Collection<String> notebookUuids) {
        if (notebookUuids == null) {
            return List.of();
        }

        return notebookUuids.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter((uuid) -> !uuid.isBlank())
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

        List<edu.cit.gako.brainbox.modules.quiz.dto.response.QuizQuestionResponse> questions = quiz.getQuestions().stream().map((question) -> {
            edu.cit.gako.brainbox.modules.quiz.dto.response.QuizQuestionResponse questionResponse = new edu.cit.gako.brainbox.modules.quiz.dto.response.QuizQuestionResponse();
            questionResponse.setType(question.getType());
            questionResponse.setText(question.getText());
            questionResponse.setOptions(question.getOptions());
            questionResponse.setCorrectIndex(question.getCorrectIndex());
            return questionResponse;
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

        List<edu.cit.gako.brainbox.modules.flashcard.dto.response.FlashcardCardResponse> cards = flashcard.getCards().stream().map((card) -> {
            edu.cit.gako.brainbox.modules.flashcard.dto.response.FlashcardCardResponse cardResponse = new edu.cit.gako.brainbox.modules.flashcard.dto.response.FlashcardCardResponse();
            cardResponse.setFront(card.getFront());
            cardResponse.setBack(card.getBack());
            return cardResponse;
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
