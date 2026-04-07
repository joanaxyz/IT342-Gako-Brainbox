package edu.cit.gako.brainbox.notebook.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.gako.brainbox.auth.service.UserService;
import edu.cit.gako.brainbox.notebook.dto.request.PlaylistAddNotebookRequest;
import edu.cit.gako.brainbox.notebook.dto.request.PlaylistReorderRequest;
import edu.cit.gako.brainbox.notebook.dto.request.PlaylistRequest;
import edu.cit.gako.brainbox.notebook.dto.response.NotebookOverviewResponse;
import edu.cit.gako.brainbox.notebook.dto.response.PlaylistResponse;
import edu.cit.gako.brainbox.notebook.entity.Notebook;
import edu.cit.gako.brainbox.notebook.entity.Playlist;
import edu.cit.gako.brainbox.notebook.repository.PlaylistRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final NotebookService notebookService;
    private final UserService userService;

    @Transactional
    public PlaylistResponse createPlaylist(PlaylistRequest request, Long userId) {
        Playlist playlist = new Playlist();
        playlist.setTitle(request.getTitle());
        playlist.setUser(userService.findById(userId));
        return mapToResponse(playlistRepository.save(playlist));
    }

    public List<PlaylistResponse> getPlaylistsByUser(Long userId) {
        return playlistRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PlaylistResponse getPlaylistResponseByUuid(String uuid, Long userId) {
        Playlist playlist = getPlaylistByUuidAndUserId(uuid, userId);
        return mapToResponse(playlist);
    }

    @Transactional
    public PlaylistResponse updatePlaylist(String uuid, Long userId, PlaylistRequest request) {
        Playlist playlist = getPlaylistByUuidAndUserId(uuid, userId);
        if (request.getTitle() != null) {
            playlist.setTitle(request.getTitle());
        }
        return mapToResponse(playlistRepository.save(playlist));
    }

    @Transactional
    public void deletePlaylist(String uuid, Long userId) {
        Playlist playlist = getPlaylistByUuidAndUserId(uuid, userId);
        playlistRepository.deleteById(playlist.getId());
    }

    @Transactional
    public PlaylistResponse addNotebook(String uuid, Long userId, PlaylistAddNotebookRequest request) {
        Playlist playlist = getPlaylistByUuidAndUserId(uuid, userId);
        Notebook notebook = notebookService.getNotebookByUuid(request.getNotebookUuid());
        notebook.assertOwnedBy(userId);
        if (!playlist.getQueue().contains(notebook)) {
            playlist.getQueue().add(notebook);
        }
        return mapToResponse(playlistRepository.save(playlist));
    }

    @Transactional
    public PlaylistResponse removeNotebook(String uuid, Long userId, String notebookUuid) {
        Playlist playlist = getPlaylistByUuidAndUserId(uuid, userId);
        Notebook notebook = notebookService.getNotebookByUuid(notebookUuid);
        playlist.getQueue().remove(notebook);
        if (playlist.getQueue().isEmpty()) {
            playlist.setCurrentIndex(0);
        } else if (playlist.getCurrentIndex() >= playlist.getQueue().size()) {
            playlist.setCurrentIndex(playlist.getQueue().size() - 1);
        }
        return mapToResponse(playlistRepository.save(playlist));
    }

    @Transactional
    public PlaylistResponse reorderQueue(String uuid, Long userId, PlaylistReorderRequest request) {
        Playlist playlist = getPlaylistByUuidAndUserId(uuid, userId);
        List<Notebook> reordered = request.getNotebookUuids().stream()
                .map(notebookService::getNotebookByUuid)
                .toList();
        playlist.getQueue().clear();
        playlist.getQueue().addAll(reordered);
        playlist.setCurrentIndex(0);
        return mapToResponse(playlistRepository.save(playlist));
    }

    @Transactional
    public PlaylistResponse setCurrentIndex(String uuid, Long userId, int index) {
        Playlist playlist = getPlaylistByUuidAndUserId(uuid, userId);
        if (index < 0 || index >= playlist.getQueue().size()) {
            throw new IllegalArgumentException("Index out of bounds");
        }
        playlist.setCurrentIndex(index);
        return mapToResponse(playlistRepository.save(playlist));
    }

    private Playlist getPlaylistByUuid(String uuid) {
        return playlistRepository.findByUuid(uuid)
                .orElseThrow(() -> new NoSuchElementException("Playlist not found"));
    }

    private Playlist getPlaylistByUuidAndUserId(String uuid, Long userId) {
        Playlist playlist = getPlaylistByUuid(uuid);
        playlist.assertOwnedBy(userId);
        return playlist;
    }

    private NotebookOverviewResponse mapNotebookToOverview(Notebook notebook) {
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

    private PlaylistResponse mapToResponse(Playlist playlist) {
        PlaylistResponse response = new PlaylistResponse();
        response.setUuid(playlist.getUuid());
        response.setTitle(playlist.getTitle());
        response.setCurrentIndex(playlist.getCurrentIndex());
        response.setCreatedAt(playlist.getCreatedAt());
        response.setUpdatedAt(playlist.getUpdatedAt());
        response.setQueue(playlist.getQueue().stream().map(this::mapNotebookToOverview).toList());
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
