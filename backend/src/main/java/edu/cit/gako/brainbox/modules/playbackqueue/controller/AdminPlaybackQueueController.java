package edu.cit.gako.brainbox.modules.playbackqueue.controller;

import edu.cit.gako.brainbox.modules.playbackqueue.dto.response.AdminPlaybackQueueResponse;
import edu.cit.gako.brainbox.modules.playbackqueue.dto.response.PlaybackQueueResponse;
import edu.cit.gako.brainbox.modules.playbackqueue.service.PlaybackQueueService;
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
@RequestMapping("/api/admin/playback-queues")
@RequiredArgsConstructor
public class AdminPlaybackQueueController {

    private final PlaybackQueueService playbackQueueService;

    @RequireRole(UserRole.ADMIN)
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminPlaybackQueueResponse>>> getQueues() {
        return ResponseEntity.ok(ApiResponse.success(playbackQueueService.getAllQueues()));
    }

    @RequireRole(UserRole.ADMIN)
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<PlaybackQueueResponse>> getQueue(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(playbackQueueService.getQueue(userId)));
    }

    @RequireRole(UserRole.ADMIN)
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> clearQueue(@PathVariable Long userId) {
        playbackQueueService.clearQueueAsAdmin(userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
