package edu.cit.gako.brainbox.modules.playlist.controller;

import edu.cit.gako.brainbox.modules.playlist.dto.response.PlaylistResponse;
import edu.cit.gako.brainbox.modules.playlist.service.PlaylistService;
import edu.cit.gako.brainbox.modules.user.entity.UserRole;
import edu.cit.gako.brainbox.platform.security.annotation.RequireRole;
import edu.cit.gako.brainbox.shared.controller.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/playlists")
@RequiredArgsConstructor
public class AdminPlaylistController {

    private final PlaylistService playlistService;

    @RequireRole(UserRole.ADMIN)
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlaylistResponse>>> getPlaylists() {
        return ResponseEntity.ok(ApiResponse.success(playlistService.getAllPlaylists()));
    }

    @RequireRole(UserRole.ADMIN)
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<PlaylistResponse>> getPlaylist(@PathVariable String uuid) {
        return ResponseEntity.ok(ApiResponse.success(playlistService.getPlaylistResponseByUuid(uuid)));
    }

    @RequireRole(UserRole.ADMIN)
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deletePlaylist(@PathVariable String uuid) {
        playlistService.deletePlaylist(uuid);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
