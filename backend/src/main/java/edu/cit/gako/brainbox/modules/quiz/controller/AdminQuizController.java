package edu.cit.gako.brainbox.modules.quiz.controller;

import edu.cit.gako.brainbox.modules.quiz.dto.response.QuizResponse;
import edu.cit.gako.brainbox.modules.quiz.service.QuizService;
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
@RequestMapping("/api/admin/quizzes")
@RequiredArgsConstructor
public class AdminQuizController {

    private final QuizService quizService;

    @RequireRole(UserRole.ADMIN)
    @GetMapping
    public ResponseEntity<ApiResponse<List<QuizResponse>>> getQuizzes() {
        return ResponseEntity.ok(ApiResponse.success(quizService.getAllQuizzes()));
    }

    @RequireRole(UserRole.ADMIN)
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<QuizResponse>> getQuiz(@PathVariable String uuid) {
        return ResponseEntity.ok(ApiResponse.success(quizService.getQuizResponse(uuid)));
    }

    @RequireRole(UserRole.ADMIN)
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(@PathVariable String uuid) {
        quizService.deleteQuiz(uuid);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
