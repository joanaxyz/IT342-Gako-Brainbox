package edu.cit.gako.brainbox.modules.offlinebundle.service;

import edu.cit.gako.brainbox.modules.flashcard.entity.Flashcard;
import edu.cit.gako.brainbox.modules.flashcard.entity.FlashcardCard;
import edu.cit.gako.brainbox.modules.flashcard.repository.FlashcardAttemptRepository;
import edu.cit.gako.brainbox.modules.flashcard.repository.FlashcardRepository;
import edu.cit.gako.brainbox.modules.notebook.entity.Notebook;
import edu.cit.gako.brainbox.modules.notebook.repository.NotebookRepository;
import edu.cit.gako.brainbox.modules.offlinebundle.dto.request.OfflineNotebookBundleRequest;
import edu.cit.gako.brainbox.modules.offlinebundle.dto.response.OfflineNotebookBundleResponse;
import edu.cit.gako.brainbox.modules.playlist.entity.Playlist;
import edu.cit.gako.brainbox.modules.playlist.repository.PlaylistRepository;
import edu.cit.gako.brainbox.modules.quiz.entity.Quiz;
import edu.cit.gako.brainbox.modules.quiz.entity.QuizQuestion;
import edu.cit.gako.brainbox.modules.quiz.repository.QuizAttemptRepository;
import edu.cit.gako.brainbox.modules.quiz.repository.QuizRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfflineNotebookBundleServiceTest {

    @Mock
    private NotebookRepository notebookRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private FlashcardAttemptRepository flashcardAttemptRepository;

    @Mock
    private PlaylistRepository playlistRepository;

    @InjectMocks
    private OfflineNotebookBundleService offlineNotebookBundleService;

    @Test
    void buildsOfflineBundleAcrossNotebookOwnedResources() {
        Notebook notebook = new Notebook();
        notebook.setId(1L);
        notebook.setUuid("nb-1");
        notebook.setTitle("Notebook");
        notebook.setContent("<p>Hello world</p>");
        notebook.setCreatedAt(Instant.parse("2026-04-18T00:00:00Z"));
        notebook.setUpdatedAt(Instant.parse("2026-04-18T01:00:00Z"));
        notebook.setVersion(3L);

        QuizQuestion question = new QuizQuestion();
        question.setType("multiple-choice");
        question.setText("Question?");
        question.setOptions(List.of("A", "B"));
        question.setCorrectIndex(0);

        Quiz quiz = new Quiz();
        quiz.setId(10L);
        quiz.setUuid("quiz-1");
        quiz.setTitle("Quiz");
        quiz.setDifficulty("easy");
        quiz.setNotebook(notebook);
        quiz.setQuestions(List.of(question));

        FlashcardCard card = new FlashcardCard();
        card.setFront("Front");
        card.setBack("Back");

        Flashcard flashcard = new Flashcard();
        flashcard.setId(20L);
        flashcard.setUuid("flash-1");
        flashcard.setTitle("Flashcard");
        flashcard.setNotebook(notebook);
        flashcard.setCards(List.of(card));

        Playlist playlist = new Playlist();
        playlist.setUuid("playlist-1");
        playlist.setTitle("Playlist");
        playlist.setUpdatedAt(Instant.parse("2026-04-18T02:00:00Z"));
        playlist.setQueue(List.of(notebook));

        OfflineNotebookBundleRequest request = new OfflineNotebookBundleRequest();
        request.setNotebookUuids(List.of("nb-1", "missing"));

        when(notebookRepository.findByUuidInAndUserId(List.of("nb-1", "missing"), 9L)).thenReturn(List.of(notebook));
        when(quizRepository.findByUserId(9L)).thenReturn(List.of(quiz));
        when(flashcardRepository.findByUserId(9L)).thenReturn(List.of(flashcard));
        when(playlistRepository.findByUserId(9L)).thenReturn(List.of(playlist));
        when(quizAttemptRepository.countByQuizId(10L)).thenReturn(1L);
        when(quizAttemptRepository.findBestScoreByQuizId(10L)).thenReturn(Optional.of(95));
        when(flashcardAttemptRepository.countByFlashcardId(20L)).thenReturn(2L);
        when(flashcardAttemptRepository.findBestMasteryByFlashcardId(20L)).thenReturn(Optional.of(5));

        OfflineNotebookBundleResponse response = offlineNotebookBundleService.getNotebookBundle(request, 9L);

        assertEquals(List.of("missing"), response.getMissingUuids());
        assertEquals(1, response.getNotebooks().size());
        assertEquals("nb-1", response.getNotebooks().get(0).getNotebook().getUuid());
        assertEquals(1, response.getNotebooks().get(0).getQuizzes().size());
        assertEquals(1, response.getNotebooks().get(0).getFlashcards().size());
        assertEquals(1, response.getNotebooks().get(0).getPlaylists().size());
        assertTrue(response.getNotebooks().get(0).getNotebook().getWordCount() > 0);
    }
}
