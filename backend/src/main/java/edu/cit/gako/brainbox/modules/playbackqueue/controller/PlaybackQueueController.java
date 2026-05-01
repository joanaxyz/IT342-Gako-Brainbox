package edu.cit.gako.brainbox.modules.playbackqueue.controller;

import edu.cit.gako.brainbox.modules.playbackqueue.dto.request.QueueAddNotebookRequest;
import edu.cit.gako.brainbox.modules.playbackqueue.dto.request.QueueReorderRequest;
import edu.cit.gako.brainbox.modules.playbackqueue.dto.response.PlaybackQueueResponse;
import edu.cit.gako.brainbox.modules.playbackqueue.service.PlaybackQueueService;
import edu.cit.gako.brainbox.platform.security.annotation.RequireAuth;
import edu.cit.gako.brainbox.shared.controller.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PlaybackQueueController {

    private final PlaybackQueueService queueService;

    @RequireAuth
    @GetMapping({"/api/queue", "/api/playback-queues/current"})
    public ResponseEntity<ApiResponse<PlaybackQueueResponse>> getQueue(
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(queueService.getQueue(userId)));
    }

    @RequireAuth
    @PostMapping({"/api/queue/notebooks", "/api/playback-queues/current/notebooks"})
    public ResponseEntity<ApiResponse<PlaybackQueueResponse>> addNotebook(
            @RequestBody QueueAddNotebookRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(queueService.addNotebook(userId, request)));
    }

    @RequireAuth
    @PutMapping({"/api/queue/playlist/{playlistUuid}", "/api/playback-queues/current/playlist/{playlistUuid}"})
    public ResponseEntity<ApiResponse<PlaybackQueueResponse>> selectPlaylist(
            @PathVariable String playlistUuid,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(queueService.selectPlaylist(userId, playlistUuid)));
    }

    @RequireAuth
    @DeleteMapping({"/api/queue/notebooks/{notebookUuid}", "/api/playback-queues/current/notebooks/{notebookUuid}"})
    public ResponseEntity<ApiResponse<PlaybackQueueResponse>> removeNotebook(
            @PathVariable String notebookUuid,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(queueService.removeNotebook(userId, notebookUuid)));
    }

    @RequireAuth
    @DeleteMapping({"/api/queue", "/api/playback-queues/current"})
    public ResponseEntity<ApiResponse<Void>> clearQueue(
            @RequestAttribute Long userId) {
        queueService.clearQueue(userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @RequireAuth
    @PatchMapping({"/api/queue/current-index", "/api/playback-queues/current/index"})
    public ResponseEntity<ApiResponse<PlaybackQueueResponse>> setCurrentIndex(
            @RequestParam int index,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(queueService.setCurrentIndex(userId, index)));
    }

    @RequireAuth
    @PutMapping({"/api/queue/reorder", "/api/playback-queues/current/reorder"})
    public ResponseEntity<ApiResponse<PlaybackQueueResponse>> reorderQueue(
            @RequestBody QueueReorderRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(queueService.reorderQueue(userId, request)));
    }
}
