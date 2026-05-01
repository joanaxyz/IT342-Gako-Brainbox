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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotebookDeletionCoordinator {

    private final PlaylistRepository playlistRepository;
    private final QuizRepository quizRepository;
    private final FlashcardRepository flashcardRepository;
    private final NotebookVersionRepository notebookVersionRepository;

    @Transactional
    public void detachNotebookRelations(Notebook notebook) {
        Long notebookId = notebook.getId();
        detachPlaylistReferences(notebookId);
        detachQuizReferences(notebookId);
        detachFlashcardReferences(notebookId);
        notebookVersionRepository.deleteByNotebookId(notebookId);
    }

    private void detachPlaylistReferences(Long notebookId) {
        List<Playlist> playlists = playlistRepository.findDistinctByQueueId(notebookId);
        playlists.forEach((playlist) -> {
            playlist.getQueue().removeIf((queuedNotebook) -> notebookId.equals(queuedNotebook.getId()));
            if (playlist.getQueue().isEmpty()) {
                playlist.setCurrentIndex(0);
            } else if (playlist.getCurrentIndex() >= playlist.getQueue().size()) {
                playlist.setCurrentIndex(playlist.getQueue().size() - 1);
            }
        });

        if (!playlists.isEmpty()) {
            playlistRepository.saveAll(playlists);
        }
    }

    private void detachQuizReferences(Long notebookId) {
        List<Quiz> quizzes = quizRepository.findByNotebookId(notebookId);
        quizzes.forEach((quiz) -> quiz.setNotebook(null));
        if (!quizzes.isEmpty()) {
            quizRepository.saveAll(quizzes);
        }
    }

    private void detachFlashcardReferences(Long notebookId) {
        List<Flashcard> flashcards = flashcardRepository.findByNotebookId(notebookId);
        flashcards.forEach((flashcard) -> flashcard.setNotebook(null));
        if (!flashcards.isEmpty()) {
            flashcardRepository.saveAll(flashcards);
        }
    }
}
