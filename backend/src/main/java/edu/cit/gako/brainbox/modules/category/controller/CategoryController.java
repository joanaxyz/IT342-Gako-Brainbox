package edu.cit.gako.brainbox.modules.category.controller;

import edu.cit.gako.brainbox.modules.category.dto.request.CategoryDeleteRequest;
import edu.cit.gako.brainbox.modules.category.dto.request.CategoryRequest;
import edu.cit.gako.brainbox.modules.category.dto.response.CategoryResponse;
import edu.cit.gako.brainbox.modules.category.service.CategoryService;
import edu.cit.gako.brainbox.platform.security.annotation.RequireAuth;
import edu.cit.gako.brainbox.shared.controller.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @RequireAuth
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories(@RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories(userId)));
    }

    @PostMapping
    @RequireAuth
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@RequestBody CategoryRequest categoryRequest, @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.createCategory(categoryRequest, userId)));
    }

    @RequireAuth
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable("id") Long categoryId,
            @RequestBody CategoryRequest categoryRequest,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.updateCategory(categoryId, categoryRequest, userId)));
    }

    @RequireAuth
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(@PathVariable("id") Long categoryId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryResponseById(categoryId)));
    }

    @RequireAuth
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable("id") Long categoryId,
            @RequestBody(required = false) CategoryDeleteRequest request,
            @RequestAttribute Long userId) {
        categoryService.deleteCategory(categoryId, userId, request != null && request.isDeleteNotebooks());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
