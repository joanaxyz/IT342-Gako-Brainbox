package edu.cit.gako.brainbox.notebook.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.cit.gako.brainbox.auth.service.UserService;
import edu.cit.gako.brainbox.notebook.entity.Notebook;
import edu.cit.gako.brainbox.notebook.entity.Playlist;
import edu.cit.gako.brainbox.notebook.repository.PlaylistRepository;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private NotebookService notebookService;

    @Mock
    private UserService userService;

    @InjectMocks
    private PlaylistService playlistService;

    @Test
    void getPlaylistsByUserIncludesNotebookWordCounts() {
        Notebook notebook = new Notebook();
        notebook.setUuid("nb-1");
        notebook.setTitle("Cells");
        notebook.setContent("<p>Cell walls keep plants rigid.</p>");
        notebook.setVersion(4L);
        notebook.setCreatedAt(Instant.parse("2026-04-02T00:00:00Z"));
        notebook.setUpdatedAt(Instant.parse("2026-04-02T01:00:00Z"));

        Playlist playlist = new Playlist();
        playlist.setUuid("pl-1");
        playlist.setTitle("Biology");
        playlist.setCurrentIndex(0);
        playlist.setCreatedAt(Instant.parse("2026-04-02T00:00:00Z"));
        playlist.setUpdatedAt(Instant.parse("2026-04-02T02:00:00Z"));
        playlist.setQueue(List.of(notebook));

        when(playlistRepository.findByUserId(42L)).thenReturn(List.of(playlist));

        var response = playlistService.getPlaylistsByUser(42L);

        assertEquals(1, response.size());
        assertEquals(1, response.get(0).getQueue().size());
        assertEquals(5, response.get(0).getQueue().get(0).getWordCount());
        assertEquals(4L, response.get(0).getQueue().get(0).getVersion());
    }
}
