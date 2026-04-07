package edu.cit.gako.brainbox.notebook.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.cit.gako.brainbox.auth.annotation.RequireAuth;
import edu.cit.gako.brainbox.common.dto.ApiResponse;
import edu.cit.gako.brainbox.notebook.dto.request.CategoryDeleteRequest;
import edu.cit.gako.brainbox.notebook.dto.request.CategoryRequest;
import edu.cit.gako.brainbox.notebook.dto.response.CategoryResponse;
import edu.cit.gako.brainbox.notebook.service.CategoryService;
import lombok.RequiredArgsConstructor;

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
