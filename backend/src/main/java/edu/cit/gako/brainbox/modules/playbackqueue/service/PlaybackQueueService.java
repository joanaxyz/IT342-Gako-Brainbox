package edu.cit.gako.brainbox.modules.playbackqueue.service;

import edu.cit.gako.brainbox.modules.notebook.dto.response.NotebookOverviewResponse;
import edu.cit.gako.brainbox.modules.notebook.service.NotebookService;
import edu.cit.gako.brainbox.modules.notebook.entity.Notebook;
import edu.cit.gako.brainbox.modules.playbackqueue.dto.request.QueueAddNotebookRequest;
import edu.cit.gako.brainbox.modules.playbackqueue.dto.request.QueueReorderRequest;
import edu.cit.gako.brainbox.modules.playbackqueue.dto.response.AdminPlaybackQueueResponse;
import edu.cit.gako.brainbox.modules.playbackqueue.dto.response.PlaybackQueueResponse;
import edu.cit.gako.brainbox.modules.playbackqueue.entity.PlaybackQueue;
import edu.cit.gako.brainbox.modules.playbackqueue.repository.PlaybackQueueRepository;
import edu.cit.gako.brainbox.modules.playlist.dto.request.PlaylistAddNotebookRequest;
import edu.cit.gako.brainbox.modules.playlist.dto.request.PlaylistReorderRequest;
import edu.cit.gako.brainbox.modules.playlist.service.PlaylistService;
import edu.cit.gako.brainbox.modules.playlist.entity.Playlist;
import edu.cit.gako.brainbox.modules.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaybackQueueService {

    private final PlaybackQueueRepository queueRepository;
    private final PlaylistService playlistService;
    private final NotebookService notebookService;
    private final UserService userService;

    public PlaybackQueueResponse getQueue(Long userId) {
        return queueRepository.findByUserId(userId)
                .map(this::mapToResponse)
                .orElseGet(() -> emptyResponse());
    }

    public List<AdminPlaybackQueueResponse> getAllQueues() {
        return queueRepository.findAll().stream()
                .map(this::mapToAdminResponse)
                .toList();
    }

    @Transactional
    public PlaybackQueueResponse selectPlaylist(Long userId, String playlistUuid) {
        PlaybackQueue queue = getOrCreate(userId);
        Playlist playlist = playlistService.getPlaylistByUuidAndUserId(playlistUuid, userId);
        queue.setSelectedPlaylist(playlist);
        queue.setCurrentIndex(0);
        queueRepository.save(queue);
        return mapPlaylistToResponse(playlist);
    }

    @Transactional
    public PlaybackQueueResponse addNotebook(Long userId, QueueAddNotebookRequest request) {
        PlaybackQueue queue = getOrCreate(userId);
        if (queue.getSelectedPlaylist() != null) {
            Playlist playlist = getSelectedPlaylistOrThrow(userId);
            PlaylistAddNotebookRequest playlistRequest = new PlaylistAddNotebookRequest();
            playlistRequest.setNotebookUuid(request.getNotebookUuid());
            playlistService.addNotebook(playlist.getUuid(), userId, playlistRequest);
            return mapToResponse(getOrCreate(userId));
        }

        Notebook notebook = notebookService.getNotebookByUuidAndUserId(request.getNotebookUuid(), userId);
        if (!queue.getQueue().contains(notebook)) {
            queue.getQueue().add(notebook);
        }
        normalizeCurrentIndex(queue);
        return mapToResponse(queueRepository.save(queue));
    }

    @Transactional
    public PlaybackQueueResponse removeNotebook(Long userId, String notebookUuid) {
        PlaybackQueue queue = getOrCreate(userId);
        if (queue.getSelectedPlaylist() != null) {
            Playlist playlist = getSelectedPlaylistOrThrow(userId);
            playlistService.removeNotebook(playlist.getUuid(), userId, notebookUuid);
            return mapToResponse(getOrCreate(userId));
        }

        Notebook notebook = notebookService.getNotebookByUuidAndUserId(notebookUuid, userId);
        queue.getQueue().remove(notebook);
        normalizeCurrentIndex(queue);
        return mapToResponse(queueRepository.save(queue));
    }

    @Transactional
    public void clearQueue(Long userId) {
        queueRepository.findByUserId(userId).ifPresent(queue -> {
            queue.setSelectedPlaylist(null);
            queue.getQueue().clear();
            queue.setCurrentIndex(0);
            queueRepository.save(queue);
        });
    }

    @Transactional
    public void clearQueueAsAdmin(Long userId) {
        clearQueue(userId);
    }

    @Transactional
    public PlaybackQueueResponse reorderQueue(Long userId, QueueReorderRequest request) {
        PlaybackQueue queue = getOrCreate(userId);
        if (queue.getSelectedPlaylist() != null) {
            Playlist playlist = getSelectedPlaylistOrThrow(userId);
            PlaylistReorderRequest playlistRequest = new PlaylistReorderRequest();
            playlistRequest.setNotebookUuids(request.getNotebookUuids());
            playlistService.reorderQueue(playlist.getUuid(), userId, playlistRequest);
            return mapToResponse(getOrCreate(userId));
        }

        List<Notebook> reordered = request.getNotebookUuids().stream()
                .map(uuid -> notebookService.getNotebookByUuidAndUserId(uuid, userId))
                .toList();
        queue.getQueue().clear();
        queue.getQueue().addAll(reordered);
        normalizeCurrentIndex(queue);
        return mapToResponse(queueRepository.save(queue));
    }

    @Transactional
    public PlaybackQueueResponse setCurrentIndex(Long userId, int index) {
        PlaybackQueue queue = getOrCreate(userId);
        if (queue.getSelectedPlaylist() != null) {
            playlistService.setCurrentIndex(queue.getSelectedPlaylist().getUuid(), userId, index);
            return mapToResponse(getOrCreate(userId));
        }

        if (queue.getQueue().isEmpty()) {
            queue.setCurrentIndex(0);
        } else if (index < 0 || index >= queue.getQueue().size()) {
            throw new IllegalArgumentException("Index out of bounds");
        } else {
            queue.setCurrentIndex(index);
        }
        return mapToResponse(queueRepository.save(queue));
    }

    private PlaybackQueue getOrCreate(Long userId) {
        return queueRepository.findByUserId(userId).orElseGet(() -> {
            PlaybackQueue queue = new PlaybackQueue();
            queue.setUser(userService.findById(userId));
            return queueRepository.save(queue);
        });
    }

    private Playlist getSelectedPlaylistOrThrow(Long userId) {
        PlaybackQueue queue = getOrCreate(userId);
        Playlist playlist = queue.getSelectedPlaylist();
        if (playlist == null) {
            throw new IllegalStateException("No playback playlist selected");
        }
        playlist.assertOwnedBy(userId);
        return playlist;
    }

    private PlaybackQueueResponse mapToResponse(PlaybackQueue queue) {
        Playlist playlist = queue.getSelectedPlaylist();
        if (playlist != null) {
            return mapPlaylistToResponse(playlist);
        }

        PlaybackQueueResponse response = new PlaybackQueueResponse();
        response.setCurrentIndex(queue.getCurrentIndex());
        response.setItems(queue.getQueue().stream().map(this::mapNotebook).toList());
        return response;
    }

    private AdminPlaybackQueueResponse mapToAdminResponse(PlaybackQueue queue) {
        AdminPlaybackQueueResponse response = new AdminPlaybackQueueResponse();
        response.setUserId(queue.getUser().getId());
        response.setUsername(queue.getUser().getUsername());
        Playlist playlist = queue.getSelectedPlaylist();
        if (playlist != null) {
            response.setPlaylistUuid(playlist.getUuid());
            response.setPlaylistTitle(playlist.getTitle());
            response.setCurrentIndex(playlist.getCurrentIndex());
            response.setItems(playlist.getQueue().stream().map(this::mapNotebook).toList());
        } else {
            response.setCurrentIndex(queue.getCurrentIndex());
            response.setItems(queue.getQueue().stream().map(this::mapNotebook).toList());
        }
        return response;
    }

    private PlaybackQueueResponse emptyResponse() {
        return mapPlaylistToResponse(null);
    }

    private PlaybackQueueResponse mapPlaylistToResponse(Playlist playlist) {
        PlaybackQueueResponse response = new PlaybackQueueResponse();
        if (playlist == null) {
            response.setCurrentIndex(0);
            response.setItems(List.of());
            return response;
        }

        response.setPlaylistUuid(playlist.getUuid());
        response.setPlaylistTitle(playlist.getTitle());
        response.setCurrentIndex(playlist.getCurrentIndex());
        response.setItems(playlist.getQueue().stream().map(this::mapNotebook).toList());
        return response;
    }

    private void normalizeCurrentIndex(PlaybackQueue queue) {
        if (queue.getQueue().isEmpty()) {
            queue.setCurrentIndex(0);
        } else if (queue.getCurrentIndex() >= queue.getQueue().size()) {
            queue.setCurrentIndex(queue.getQueue().size() - 1);
        } else if (queue.getCurrentIndex() < 0) {
            queue.setCurrentIndex(0);
        }
    }

    private NotebookOverviewResponse mapNotebook(Notebook notebook) {
        NotebookOverviewResponse r = new NotebookOverviewResponse();
        r.setUuid(notebook.getUuid());
        r.setTitle(notebook.getTitle());
        r.setWordCount(countWords(notebook.getContent()));
        r.setCreatedAt(notebook.getCreatedAt());
        r.setUpdatedAt(notebook.getUpdatedAt());
        r.setLastReviewedAt(notebook.getLastReviewedAt());
        r.setVersion(notebook.getVersion() != null ? notebook.getVersion() : 0L);
        if (notebook.getCategory() != null) {
            r.setCategoryId(notebook.getCategory().getId());
            r.setCategoryName(notebook.getCategory().getName());
        }
        return r;
    }

    private int countWords(String html) {
        if (html == null || html.isBlank()) return 0;
        String plainText = html
                .replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replaceAll("&[^;]+;", " ")
                .trim();
        if (plainText.isBlank()) return 0;
        return plainText.split("\\s+").length;
    }
}
