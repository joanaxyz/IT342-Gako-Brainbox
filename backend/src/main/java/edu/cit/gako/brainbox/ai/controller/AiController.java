package edu.cit.gako.brainbox.ai.controller;

import edu.cit.gako.brainbox.ai.dto.request.AiRequest;
import edu.cit.gako.brainbox.ai.dto.response.AiResponse;
import edu.cit.gako.brainbox.ai.service.AiService;
import edu.cit.gako.brainbox.auth.annotation.RequireAuth;
import edu.cit.gako.brainbox.common.dto.ApiResponse;
import edu.cit.gako.brainbox.notebook.service.NotebookService;
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
