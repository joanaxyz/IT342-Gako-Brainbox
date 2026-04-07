package edu.cit.gako.brainbox.ai.controller;

import edu.cit.gako.brainbox.ai.dto.request.AiConfigRequest;
import edu.cit.gako.brainbox.ai.dto.response.AiConfigListResponse;
import edu.cit.gako.brainbox.ai.dto.response.AiConfigResponse;
import edu.cit.gako.brainbox.ai.service.AiConfigService;
import edu.cit.gako.brainbox.auth.annotation.RequireAuth;
import edu.cit.gako.brainbox.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/config")
@RequiredArgsConstructor
public class AiConfigController {

    private final AiConfigService aiConfigService;

    @RequireAuth
    @GetMapping
    public ResponseEntity<ApiResponse<AiConfigResponse>> getConfig(@RequestAttribute Long userId) {
        return aiConfigService.getConfig(userId)
            .map((config) -> ResponseEntity.ok(ApiResponse.success(config)))
            .orElse(ResponseEntity.ok(ApiResponse.success(null)));
    }

    @RequireAuth
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<AiConfigListResponse>> listConfigs(@RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(aiConfigService.listConfigs(userId)));
    }

    @RequireAuth
    @PutMapping
    public ResponseEntity<ApiResponse<AiConfigResponse>> saveConfig(
            @RequestBody AiConfigRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(aiConfigService.saveConfig(request, userId)));
    }

    @RequireAuth
    @PutMapping("/{id}/select")
    public ResponseEntity<ApiResponse<AiConfigResponse>> selectConfig(
            @PathVariable Long id,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(aiConfigService.selectConfig(userId, id)));
    }

    @RequireAuth
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(
            @PathVariable Long id,
            @RequestAttribute Long userId) {
        aiConfigService.deleteConfig(userId, id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
