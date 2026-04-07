package edu.cit.gako.brainbox.notebook.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.cit.gako.brainbox.auth.service.UserService;
import edu.cit.gako.brainbox.notebook.dto.request.NotebookRequest;
import edu.cit.gako.brainbox.notebook.entity.Notebook;
import edu.cit.gako.brainbox.notebook.repository.CategoryRepository;
import edu.cit.gako.brainbox.notebook.repository.FlashcardRepository;
import edu.cit.gako.brainbox.notebook.repository.NotebookRepository;
import edu.cit.gako.brainbox.notebook.repository.NotebookVersionRepository;
import edu.cit.gako.brainbox.notebook.repository.PlaylistRepository;
import edu.cit.gako.brainbox.notebook.repository.QuizRepository;

@ExtendWith(MockitoExtension.class)
class NotebookServiceTest {

    @Mock
    private NotebookRepository notebookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotebookVersionSnapshotService notebookVersionSnapshotService;

    @Mock
    private NotebookVersionRepository notebookVersionRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private PlaylistRepository playlistRepository;

    @InjectMocks
    private NotebookService notebookService;

    @Test
    void saveContentUpdatesNotebookAndCreatesSnapshot() {
        Notebook notebook = createNotebook("nb-1", "Server copy", "<p>server</p>", 5L);
        when(notebookRepository.findByUuidAndUserId("nb-1", 42L)).thenReturn(Optional.of(notebook));
        when(notebookRepository.saveAndFlush(any(Notebook.class))).thenAnswer((invocation) -> invocation.getArgument(0));

        var response = notebookService.saveContent("nb-1", 42L, "<p>local draft</p>");

        assertEquals("<p>local draft</p>", response.getContent());
        assertEquals("<p>local draft</p>", notebook.getContent());
        verify(notebookRepository).saveAndFlush(notebook);
        verify(notebookVersionSnapshotService).createSnapshot(notebook, "<p>local draft</p>");
    }

    @Test
    void updateNotebookAllowsTitleChangesWithoutVersionMatching() {
        Notebook notebook = createNotebook("nb-2", "Original title", "<p>server</p>", 3L);
        when(notebookRepository.findByUuidAndUserId("nb-2", 7L)).thenReturn(Optional.of(notebook));
        when(notebookRepository.saveAndFlush(any(Notebook.class))).thenAnswer((invocation) -> invocation.getArgument(0));

        NotebookRequest request = new NotebookRequest();
        request.setTitle("Renamed notebook");

        var response = notebookService.updateNotebook("nb-2", 7L, request);

        assertEquals("Renamed notebook", response.getTitle());
        assertEquals("Renamed notebook", notebook.getTitle());
        verify(notebookRepository).saveAndFlush(notebook);
    }

    private Notebook createNotebook(String uuid, String title, String content, Long version) {
        Notebook notebook = new Notebook();
        notebook.setUuid(uuid);
        notebook.setTitle(title);
        notebook.setContent(content);
        notebook.setVersion(version);
        notebook.setCreatedAt(Instant.parse("2026-04-02T00:00:00Z"));
        notebook.setUpdatedAt(Instant.parse("2026-04-02T00:00:00Z"));
        return notebook;
    }
}
