package com.example.brainbox_api.notebook.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.example.brainbox_api.notebook.dto.request.CategoryRequest;
import com.example.brainbox_api.notebook.dto.response.CategoryResponse;
import com.example.brainbox_api.notebook.entity.Category;
import com.example.brainbox_api.notebook.repository.CategoryRepository;
import com.example.brainbox_api.auth.entity.User;
import com.example.brainbox_api.auth.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
            .map(this::mapToResponse)
            .toList();
    }

    public List<CategoryResponse> getCategoriesByUser(User user) {
        return categoryRepository.findAll().stream()
            .filter(c -> c.getUser().getId().equals(user.getId()))
            .map(this::mapToResponse)
            .toList();
    }

    public CategoryResponse createCategory(CategoryRequest request, Long userId) {
        User user = userService.findById(userId);
        Category category = new Category();
        category.setName(request.getName());
        category.setUser(user);
        return mapToResponse(categoryRepository.save(category));
    }

    public CategoryResponse getCategoryResponseById(Long id) {
        return mapToResponse(getCategoryById(id));
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    private CategoryResponse mapToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        return response;
    }
}
