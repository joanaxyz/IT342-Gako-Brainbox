package com.example.brainbox_api.notebook.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.brainbox_api.auth.annotation.RequireAuth;
import com.example.brainbox_api.common.dto.ApiResponse;
import com.example.brainbox_api.notebook.dto.request.CategoryRequest;
import com.example.brainbox_api.notebook.dto.response.CategoryResponse;
import com.example.brainbox_api.notebook.service.CategoryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories()));
    }

    @PostMapping
    @RequireAuth
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@RequestBody CategoryRequest categoryRequest, @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.createCategory(categoryRequest, userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryResponseById(id)));
    }

    @RequireAuth
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
