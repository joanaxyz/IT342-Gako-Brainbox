package edu.cit.gako.brainbox.modules.notebook.service;

import edu.cit.gako.brainbox.modules.flashcard.entity.Flashcard;
import edu.cit.gako.brainbox.modules.flashcard.repository.FlashcardRepository;
import edu.cit.gako.brainbox.modules.notebook.entity.Notebook;
import edu.cit.gako.brainbox.modules.notebook.repository.NotebookVersionRepository;
import edu.cit.gako.brainbox.modules.playlist.entity.Playlist;
import edu.cit.gako.brainbox.modules.playlist.repository.PlaylistRepository;
import edu.cit.gako.brainbox.modules.quiz.entity.Quiz;
import edu.cit.gako.brainbox.modules.quiz.repository.QuizRepository;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotebookDeletionCoordinatorTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private NotebookVersionRepository notebookVersionRepository;

    @InjectMocks
    private NotebookDeletionCoordinator notebookDeletionCoordinator;

    @Test
    void detachesAllOwnedRelationsBeforeDelete() {
        Notebook notebook = new Notebook();
        notebook.setId(44L);

        Playlist playlist = new Playlist();
        playlist.setCurrentIndex(2);
        playlist.setQueue(new java.util.ArrayList<>(List.of(notebook)));

        Quiz quiz = new Quiz();
        quiz.setNotebook(notebook);

        Flashcard flashcard = new Flashcard();
        flashcard.setNotebook(notebook);

        when(playlistRepository.findDistinctByQueueId(44L)).thenReturn(List.of(playlist));
        when(quizRepository.findByNotebookId(44L)).thenReturn(List.of(quiz));
        when(flashcardRepository.findByNotebookId(44L)).thenReturn(List.of(flashcard));

        notebookDeletionCoordinator.detachNotebookRelations(notebook);

        assertEquals(0, playlist.getQueue().size());
        assertEquals(0, playlist.getCurrentIndex());
        assertNull(quiz.getNotebook());
        assertNull(flashcard.getNotebook());

        verify(playlistRepository).saveAll(List.of(playlist));
        verify(quizRepository).saveAll(List.of(quiz));
        verify(flashcardRepository).saveAll(List.of(flashcard));
        verify(notebookVersionRepository).deleteByNotebookId(44L);
    }
}
