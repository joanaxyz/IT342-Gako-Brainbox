package edu.cit.gako.brainbox.notebook.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.cit.gako.brainbox.auth.annotation.RequireAuth;
import edu.cit.gako.brainbox.common.dto.ApiResponse;
import edu.cit.gako.brainbox.notebook.dto.request.FlashcardAttemptRequest;
import edu.cit.gako.brainbox.notebook.dto.request.FlashcardRequest;
import edu.cit.gako.brainbox.notebook.dto.response.FlashcardResponse;
import edu.cit.gako.brainbox.notebook.service.FlashcardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    @RequireAuth
    @PostMapping
    public ResponseEntity<ApiResponse<FlashcardResponse>> createFlashcard(
            @RequestBody FlashcardRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(flashcardService.createFlashcard(request, userId)));
    }

    @RequireAuth
    @GetMapping
    public ResponseEntity<ApiResponse<List<FlashcardResponse>>> getFlashcards(@RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(flashcardService.getFlashcardsByUser(userId)));
    }

    @RequireAuth
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<FlashcardResponse>> getFlashcard(
            @PathVariable String uuid,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(flashcardService.getFlashcardResponse(uuid, userId)));
    }

    @RequireAuth
    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<FlashcardResponse>> updateFlashcard(
            @PathVariable String uuid,
            @RequestBody FlashcardRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(flashcardService.updateFlashcard(uuid, userId, request)));
    }

    @RequireAuth
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteFlashcard(
            @PathVariable String uuid,
            @RequestAttribute Long userId) {
        flashcardService.deleteFlashcard(uuid, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @RequireAuth
    @PostMapping("/{uuid}/attempts")
    public ResponseEntity<ApiResponse<FlashcardResponse>> recordAttempt(
            @PathVariable String uuid,
            @RequestBody FlashcardAttemptRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(flashcardService.recordAttempt(uuid, userId, request)));
    }
}
