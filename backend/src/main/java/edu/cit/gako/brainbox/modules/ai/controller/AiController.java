package edu.cit.gako.brainbox.modules.ai.controller;

import edu.cit.gako.brainbox.modules.ai.dto.request.AiRequest;
import edu.cit.gako.brainbox.modules.ai.dto.response.AiResponse;
import edu.cit.gako.brainbox.modules.ai.service.AiService;
import edu.cit.gako.brainbox.modules.notebook.service.NotebookService;
import edu.cit.gako.brainbox.platform.security.annotation.RequireAuth;
import edu.cit.gako.brainbox.shared.controller.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final NotebookService notebookService;

    @RequireAuth
    @PostMapping("/query")
    public ResponseEntity<ApiResponse<AiResponse>> queryAi(@RequestBody AiRequest aiRequest, @RequestAttribute Long userId) {
        notebookService.getNotebookByUuidAndUserId(aiRequest.getNotebookUuid(), userId);
        return ResponseEntity.ok(ApiResponse.success(aiService.generateResponse(aiRequest, userId)));
    }

}
