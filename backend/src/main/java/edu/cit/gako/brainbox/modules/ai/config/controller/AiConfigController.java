package edu.cit.gako.brainbox.modules.ai.config.controller;

import edu.cit.gako.brainbox.modules.ai.config.dto.request.AiConfigRequest;
import edu.cit.gako.brainbox.modules.ai.config.dto.response.AiConfigListResponse;
import edu.cit.gako.brainbox.modules.ai.config.dto.response.AiConfigResponse;
import edu.cit.gako.brainbox.modules.ai.config.service.AiConfigService;
import edu.cit.gako.brainbox.platform.security.annotation.RequireAuth;
import edu.cit.gako.brainbox.shared.controller.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AiConfigController {

    private final AiConfigService aiConfigService;

    @RequireAuth
    @GetMapping({"/api/ai/config", "/api/ai/configs/selected"})
    public ResponseEntity<ApiResponse<AiConfigResponse>> getConfig(@RequestAttribute Long userId) {
        return aiConfigService.getConfig(userId)
            .map((config) -> ResponseEntity.ok(ApiResponse.success(config)))
            .orElse(ResponseEntity.ok(ApiResponse.success(null)));
    }

    @RequireAuth
    @GetMapping({"/api/ai/config/list", "/api/ai/configs"})
    public ResponseEntity<ApiResponse<AiConfigListResponse>> listConfigs(@RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(aiConfigService.listConfigs(userId)));
    }

    @RequireAuth
    @PutMapping({"/api/ai/config", "/api/ai/configs"})
    public ResponseEntity<ApiResponse<AiConfigResponse>> saveConfig(
            @RequestBody AiConfigRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(aiConfigService.saveConfig(request, userId)));
    }

    @RequireAuth
    @PutMapping({"/api/ai/config/{id}/select", "/api/ai/configs/{id}/selected"})
    public ResponseEntity<ApiResponse<AiConfigResponse>> selectConfig(
            @PathVariable Long id,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(aiConfigService.selectConfig(userId, id)));
    }

    @RequireAuth
    @DeleteMapping({"/api/ai/config/{id}", "/api/ai/configs/{id}"})
    public ResponseEntity<ApiResponse<Void>> deleteConfig(
            @PathVariable Long id,
            @RequestAttribute Long userId) {
        aiConfigService.deleteConfig(userId, id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
