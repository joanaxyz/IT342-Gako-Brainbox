package edu.cit.gako.brainbox.notebook.controller;

import edu.cit.gako.brainbox.auth.annotation.RequireAuth;
import edu.cit.gako.brainbox.common.dto.ApiResponse;
import edu.cit.gako.brainbox.notebook.dto.request.QueueAddNotebookRequest;
import edu.cit.gako.brainbox.notebook.dto.request.QueueReorderRequest;
import edu.cit.gako.brainbox.notebook.dto.response.PlaybackQueueResponse;
import edu.cit.gako.brainbox.notebook.service.PlaybackQueueService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class PlaybackQueueController {

    private final PlaybackQueueService queueService;

    @RequireAuth
    @GetMapping
    public ResponseEntity<ApiResponse<PlaybackQueueResponse>> getQueue(
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(queueService.getQueue(userId)));
    }

    @RequireAuth
    @PostMapping("/notebooks")
    public ResponseEntity<ApiResponse<PlaybackQueueResponse>> addNotebook(
            @RequestBody QueueAddNotebookRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(queueService.addNotebook(userId, request)));
    }

    @RequireAuth
    @DeleteMapping("/notebooks/{notebookUuid}")
    public ResponseEntity<ApiResponse<PlaybackQueueResponse>> removeNotebook(
            @PathVariable String notebookUuid,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(queueService.removeNotebook(userId, notebookUuid)));
    }

    @RequireAuth
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearQueue(
            @RequestAttribute Long userId) {
        queueService.clearQueue(userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @RequireAuth
    @PutMapping("/reorder")
    public ResponseEntity<ApiResponse<PlaybackQueueResponse>> reorderQueue(
            @RequestBody QueueReorderRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(queueService.reorderQueue(userId, request)));
    }
}
