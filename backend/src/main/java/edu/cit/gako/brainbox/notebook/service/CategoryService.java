package edu.cit.gako.brainbox.notebook.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.gako.brainbox.auth.entity.User;
import edu.cit.gako.brainbox.auth.service.UserService;
import edu.cit.gako.brainbox.notebook.dto.request.CategoryRequest;
import edu.cit.gako.brainbox.notebook.dto.response.CategoryResponse;
import edu.cit.gako.brainbox.notebook.entity.Category;
import edu.cit.gako.brainbox.notebook.entity.Notebook;
import edu.cit.gako.brainbox.notebook.repository.CategoryRepository;
import edu.cit.gako.brainbox.notebook.repository.NotebookRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final NotebookRepository notebookRepository;
    private final NotebookService notebookService;
    private final UserService userService;

    public List<CategoryResponse> getAllCategories(Long userId) {
        return categoryRepository.findByUserId(userId).stream()
            .map(this::mapToResponse)
            .toList();
    }

    public List<CategoryResponse> getCategoriesByUser(User user) {
        return categoryRepository.findByUserId(user.getId()).stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, Long userId) {
        User user = userService.findById(userId);
        Category category = new Category();
        category.setName(request.getName());
        category.setUser(user);
        return mapToResponse(categoryRepository.save(category));
    }

    public CategoryResponse getCategoryResponseById(Long categoryId) {
        return mapToResponse(getCategoryById(categoryId));
    }

    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Transactional
    public void deleteCategory(Long categoryId, Long userId, boolean deleteNotebooks) {
        Category category = getCategoryById(categoryId);
        category.assertOwnedBy(userId);

        if (deleteNotebooks) {
            List<Notebook> notebooks = notebookRepository.findByCategoryIdAndUserId(categoryId, userId);
            notebooks.forEach(notebookService::deleteNotebook);
        } else {
            notebookRepository.clearCategoryByCategoryIdAndUserId(categoryId, userId);
        }

        categoryRepository.delete(category);
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
