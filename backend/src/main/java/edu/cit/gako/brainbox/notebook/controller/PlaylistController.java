package edu.cit.gako.brainbox.notebook.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.cit.gako.brainbox.auth.annotation.RequireAuth;
import edu.cit.gako.brainbox.common.dto.ApiResponse;
import edu.cit.gako.brainbox.notebook.dto.request.PlaylistAddNotebookRequest;
import edu.cit.gako.brainbox.notebook.dto.request.PlaylistReorderRequest;
import edu.cit.gako.brainbox.notebook.dto.request.PlaylistRequest;
import edu.cit.gako.brainbox.notebook.dto.response.PlaylistResponse;
import edu.cit.gako.brainbox.notebook.service.PlaylistService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @RequireAuth
    @PostMapping
    public ResponseEntity<ApiResponse<PlaylistResponse>> createPlaylist(
            @RequestBody PlaylistRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.createPlaylist(request, userId)));
    }

    @RequireAuth
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlaylistResponse>>> getPlaylists(@RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.getPlaylistsByUser(userId)));
    }

    @RequireAuth
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<PlaylistResponse>> getPlaylist(
            @PathVariable String uuid,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.getPlaylistResponseByUuid(uuid, userId)));
    }

    @RequireAuth
    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<PlaylistResponse>> updatePlaylist(
            @PathVariable String uuid,
            @RequestBody PlaylistRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.updatePlaylist(uuid, userId, request)));
    }

    @RequireAuth
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deletePlaylist(
            @PathVariable String uuid,
            @RequestAttribute Long userId) {
        playlistService.deletePlaylist(uuid, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @RequireAuth
    @PostMapping("/{uuid}/notebooks")
    public ResponseEntity<ApiResponse<PlaylistResponse>> addNotebook(
            @PathVariable String uuid,
            @RequestBody PlaylistAddNotebookRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.addNotebook(uuid, userId, request)));
    }

    @RequireAuth
    @DeleteMapping("/{uuid}/notebooks/{notebookUuid}")
    public ResponseEntity<ApiResponse<PlaylistResponse>> removeNotebook(
            @PathVariable String uuid,
            @PathVariable String notebookUuid,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.removeNotebook(uuid, userId, notebookUuid)));
    }

    @RequireAuth
    @PutMapping("/{uuid}/reorder")
    public ResponseEntity<ApiResponse<PlaylistResponse>> reorderQueue(
            @PathVariable String uuid,
            @RequestBody PlaylistReorderRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.reorderQueue(uuid, userId, request)));
    }

    @RequireAuth
    @PatchMapping("/{uuid}/current-index")
    public ResponseEntity<ApiResponse<PlaylistResponse>> setCurrentIndex(
            @PathVariable String uuid,
            @RequestParam int index,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.setCurrentIndex(uuid, userId, index)));
    }
}
