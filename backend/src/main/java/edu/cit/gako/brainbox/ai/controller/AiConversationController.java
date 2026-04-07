package edu.cit.gako.brainbox.ai.controller;

import edu.cit.gako.brainbox.ai.dto.request.AiConversationRequest;
import edu.cit.gako.brainbox.ai.dto.response.AiConversationResponse;
import edu.cit.gako.brainbox.ai.service.AiConversationService;
import edu.cit.gako.brainbox.auth.annotation.RequireAuth;
import edu.cit.gako.brainbox.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/conversations")
@RequiredArgsConstructor
public class AiConversationController {

    private final AiConversationService service;

    @RequireAuth
    @GetMapping
    public ResponseEntity<ApiResponse<List<AiConversationResponse>>> getConversations(
            @RequestParam String notebookUuid,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(service.getConversations(userId, notebookUuid)));
    }

    @RequireAuth
    @PostMapping
    public ResponseEntity<ApiResponse<AiConversationResponse>> saveConversation(
            @RequestBody AiConversationRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(service.save(userId, request)));
    }

    @RequireAuth
    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<AiConversationResponse>> updateConversation(
            @PathVariable String uuid,
            @RequestBody AiConversationRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(service.update(userId, uuid, request)));
    }

    @RequireAuth
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @PathVariable String uuid,
            @RequestAttribute Long userId) {
        service.delete(userId, uuid);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
