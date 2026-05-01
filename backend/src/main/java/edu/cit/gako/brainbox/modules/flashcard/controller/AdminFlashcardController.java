package edu.cit.gako.brainbox.modules.flashcard.controller;

import edu.cit.gako.brainbox.modules.flashcard.dto.response.FlashcardResponse;
import edu.cit.gako.brainbox.modules.flashcard.service.FlashcardService;
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
@RequestMapping("/api/admin/flashcards")
@RequiredArgsConstructor
public class AdminFlashcardController {

    private final FlashcardService flashcardService;

    @RequireRole(UserRole.ADMIN)
    @GetMapping
    public ResponseEntity<ApiResponse<List<FlashcardResponse>>> getFlashcards() {
        return ResponseEntity.ok(ApiResponse.success(flashcardService.getAllFlashcards()));
    }

    @RequireRole(UserRole.ADMIN)
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<FlashcardResponse>> getFlashcard(@PathVariable String uuid) {
        return ResponseEntity.ok(ApiResponse.success(flashcardService.getFlashcardResponse(uuid)));
    }

    @RequireRole(UserRole.ADMIN)
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteFlashcard(@PathVariable String uuid) {
        flashcardService.deleteFlashcard(uuid);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
