package edu.cit.gako.brainbox.modules.ai.conversation.controller;

import edu.cit.gako.brainbox.modules.ai.conversation.dto.response.AiConversationResponse;
import edu.cit.gako.brainbox.modules.ai.conversation.service.AiConversationService;
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
@RequestMapping("/api/admin/ai-conversations")
@RequiredArgsConstructor
public class AdminAiConversationController {

    private final AiConversationService aiConversationService;

    @RequireRole(UserRole.ADMIN)
    @GetMapping
    public ResponseEntity<ApiResponse<List<AiConversationResponse>>> getConversations() {
        return ResponseEntity.ok(ApiResponse.success(aiConversationService.getAllConversations()));
    }

    @RequireRole(UserRole.ADMIN)
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<AiConversationResponse>> getConversation(@PathVariable String uuid) {
        return ResponseEntity.ok(ApiResponse.success(aiConversationService.getConversation(uuid)));
    }

    @RequireRole(UserRole.ADMIN)
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(@PathVariable String uuid) {
        aiConversationService.deleteAsAdmin(uuid);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
