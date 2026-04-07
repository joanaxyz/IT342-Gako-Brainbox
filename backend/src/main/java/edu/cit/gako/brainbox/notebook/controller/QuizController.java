package edu.cit.gako.brainbox.notebook.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.cit.gako.brainbox.auth.annotation.RequireAuth;
import edu.cit.gako.brainbox.common.dto.ApiResponse;
import edu.cit.gako.brainbox.notebook.dto.request.QuizAttemptRequest;
import edu.cit.gako.brainbox.notebook.dto.request.QuizRequest;
import edu.cit.gako.brainbox.notebook.dto.response.QuizResponse;
import edu.cit.gako.brainbox.notebook.service.QuizService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @RequireAuth
    @PostMapping
    public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(
            @RequestBody QuizRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(quizService.createQuiz(request, userId)));
    }

    @RequireAuth
    @GetMapping
    public ResponseEntity<ApiResponse<List<QuizResponse>>> getQuizzes(@RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getQuizzesByUser(userId)));
    }

    @RequireAuth
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<QuizResponse>> getQuiz(
            @PathVariable String uuid,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getQuizResponse(uuid, userId)));
    }

    @RequireAuth
    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<QuizResponse>> updateQuiz(
            @PathVariable String uuid,
            @RequestBody QuizRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(quizService.updateQuiz(uuid, userId, request)));
    }

    @RequireAuth
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(
            @PathVariable String uuid,
            @RequestAttribute Long userId) {
        quizService.deleteQuiz(uuid, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @RequireAuth
    @PostMapping("/{uuid}/attempts")
    public ResponseEntity<ApiResponse<QuizResponse>> recordAttempt(
            @PathVariable String uuid,
            @RequestBody QuizAttemptRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(quizService.recordAttempt(uuid, userId, request)));
    }
}
