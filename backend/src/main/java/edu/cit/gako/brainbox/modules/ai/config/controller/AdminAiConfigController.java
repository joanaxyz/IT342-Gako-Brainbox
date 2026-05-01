package edu.cit.gako.brainbox.modules.ai.config.controller;

import edu.cit.gako.brainbox.modules.ai.config.dto.response.AiConfigResponse;
import edu.cit.gako.brainbox.modules.ai.config.service.AiConfigService;
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
@RequestMapping("/api/admin/ai-configs")
@RequiredArgsConstructor
public class AdminAiConfigController {

    private final AiConfigService aiConfigService;

    @RequireRole(UserRole.ADMIN)
    @GetMapping
    public ResponseEntity<ApiResponse<List<AiConfigResponse>>> getConfigs() {
        return ResponseEntity.ok(ApiResponse.success(aiConfigService.getAllConfigResponses()));
    }

    @RequireRole(UserRole.ADMIN)
    @GetMapping("/{configId}")
    public ResponseEntity<ApiResponse<AiConfigResponse>> getConfig(@PathVariable Long configId) {
        return ResponseEntity.ok(ApiResponse.success(aiConfigService.getConfigResponseById(configId)));
    }

    @RequireRole(UserRole.ADMIN)
    @DeleteMapping("/{configId}")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@PathVariable Long configId) {
        aiConfigService.deleteConfigAsAdmin(configId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
