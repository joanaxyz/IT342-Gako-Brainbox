package edu.cit.gako.brainbox.modules.notebook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.gako.brainbox.modules.category.repository.CategoryRepository;
import edu.cit.gako.brainbox.modules.notebook.dto.response.NotebookFullResponse;
import edu.cit.gako.brainbox.modules.notebook.dto.response.NotebookOverviewResponse;
import edu.cit.gako.brainbox.modules.notebook.entity.Notebook;
import edu.cit.gako.brainbox.modules.notebook.repository.NotebookMutationRecordRepository;
import edu.cit.gako.brainbox.modules.notebook.repository.NotebookRepository;
import edu.cit.gako.brainbox.modules.user.entity.User;
import edu.cit.gako.brainbox.modules.user.service.UserService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotebookServiceTest {

    @Mock
    private NotebookRepository notebookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserService userService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private NotebookMutationRecordRepository notebookMutationRecordRepository;

    @Mock
    private NotebookDeletionCoordinator notebookDeletionCoordinator;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotebookService notebookService;

    @Test
    void recentlyEditedSkipsBlankAndNewlyCreatedNotebooks() {
        Instant createdAt = Instant.parse("2026-05-26T00:00:00Z");
        Notebook edited = notebook("edited", "Edited notes", "<p>real study notes</p>", createdAt, createdAt.plusSeconds(30));
        Notebook blankEdited = notebook("blank", "Blank shell", "<p></p>", createdAt, createdAt.plusSeconds(20));
        Notebook createdOnly = notebook("created", "Created only", "<p>seed content</p>", createdAt, createdAt);

        when(notebookRepository.findByUserIdOrderByUpdatedAtDesc(11L))
                .thenReturn(List.of(blankEdited, edited, createdOnly));

        List<NotebookOverviewResponse> result = notebookService.getRecentlyEditedNotebooksByUser(11L);

        assertEquals(1, result.size());
        assertEquals("edited", result.get(0).getUuid());
    }

    @Test
    void saveContentTreatsBlankEditorShellAsUnchanged() {
        Instant createdAt = Instant.parse("2026-05-26T00:00:00Z");
        Notebook notebook = notebook("blank", "Blank notebook", "", createdAt, createdAt);
        notebook.setUser(user(11L));

        when(notebookRepository.findByUuidAndUserId("blank", 11L)).thenReturn(Optional.of(notebook));

        NotebookFullResponse response = notebookService.saveContent("blank", 11L, "<p></p>", null, null);

        assertEquals("", response.getContent());
        assertEquals(0, response.getWordCount());
        assertEquals(createdAt, response.getUpdatedAt());
        verify(notebookRepository, never()).saveAndFlush(notebook);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void notebookPrePersistUsesSameCreatedAndUpdatedTimestamp() throws Exception {
        Notebook notebook = new Notebook();
        java.lang.reflect.Method prePersist = Notebook.class.getDeclaredMethod("prePersist");
        prePersist.setAccessible(true);

        prePersist.invoke(notebook);

        assertEquals(notebook.getCreatedAt(), notebook.getUpdatedAt());
        assertEquals(0L, notebook.getVersion());
    }

    private Notebook notebook(String uuid, String title, String content, Instant createdAt, Instant updatedAt) {
        Notebook notebook = new Notebook();
        notebook.setUuid(uuid);
        notebook.setTitle(title);
        notebook.setContent(content);
        notebook.setCreatedAt(createdAt);
        notebook.setUpdatedAt(updatedAt);
        notebook.setVersion(updatedAt.isAfter(createdAt) ? 1L : 0L);
        notebook.setUser(user(11L));
        return notebook;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }
}
