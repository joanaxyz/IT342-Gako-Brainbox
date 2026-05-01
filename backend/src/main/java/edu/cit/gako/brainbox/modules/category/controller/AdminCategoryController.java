package edu.cit.gako.brainbox.modules.category.controller;

import edu.cit.gako.brainbox.modules.category.dto.response.CategoryResponse;
import edu.cit.gako.brainbox.modules.category.service.CategoryService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @RequireRole(UserRole.ADMIN)
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategoryResponses()));
    }

    @RequireRole(UserRole.ADMIN)
    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryResponseById(categoryId)));
    }

    @RequireRole(UserRole.ADMIN)
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "false") boolean deleteNotebooks) {
        categoryService.deleteCategoryAsAdmin(categoryId, deleteNotebooks);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
