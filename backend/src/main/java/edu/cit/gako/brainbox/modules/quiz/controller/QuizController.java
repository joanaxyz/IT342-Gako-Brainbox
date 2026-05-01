package edu.cit.gako.brainbox.modules.quiz.controller;

import edu.cit.gako.brainbox.modules.quiz.dto.request.QuizAttemptRequest;
import edu.cit.gako.brainbox.modules.quiz.dto.request.QuizRequest;
import edu.cit.gako.brainbox.modules.quiz.dto.response.QuizResponse;
import edu.cit.gako.brainbox.modules.quiz.service.QuizService;
import edu.cit.gako.brainbox.platform.security.annotation.RequireAuth;
import edu.cit.gako.brainbox.shared.controller.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
