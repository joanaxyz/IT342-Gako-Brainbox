package edu.cit.gako.brainbox.modules.category.service;

import edu.cit.gako.brainbox.modules.category.dto.request.CategoryRequest;
import edu.cit.gako.brainbox.modules.category.dto.response.CategoryResponse;
import edu.cit.gako.brainbox.modules.category.entity.Category;
import edu.cit.gako.brainbox.modules.category.repository.CategoryRepository;
import edu.cit.gako.brainbox.modules.notebook.service.NotebookService;
import edu.cit.gako.brainbox.modules.notebook.entity.Notebook;
import edu.cit.gako.brainbox.modules.user.service.UserService;
import edu.cit.gako.brainbox.modules.user.entity.User;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;
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

    public List<CategoryResponse> getAllCategoryResponses() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, Long userId) {
        User user = userService.findById(userId);
        Category category = new Category();
        category.setName(normalizeCategoryName(request));
        category.setUser(user);
        return mapToResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request, Long userId) {
        Category category = getCategoryById(categoryId);
        category.assertOwnedBy(userId);
        category.setName(normalizeCategoryName(request));
        return mapToResponse(categoryRepository.save(category));
    }

    public CategoryResponse getCategoryResponseById(Long categoryId) {
        return mapToResponse(getCategoryById(categoryId));
    }

    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new NoSuchElementException("Category not found"));
    }

    @Transactional
    public void deleteCategory(Long categoryId, Long userId, boolean deleteNotebooks) {
        Category category = getCategoryById(categoryId);
        category.assertOwnedBy(userId);
        deleteCategory(category, userId, deleteNotebooks);
    }

    @Transactional
    public void deleteCategoryAsAdmin(Long categoryId, boolean deleteNotebooks) {
        Category category = getCategoryById(categoryId);
        deleteCategory(category, category.getUser().getId(), deleteNotebooks);
    }

    private void deleteCategory(Category category, Long userId, boolean deleteNotebooks) {
        if (deleteNotebooks) {
            List<Notebook> notebooks = notebookService.getNotebooksByCategoryIdAndUserId(category.getId(), userId);
            notebooks.forEach(notebookService::deleteNotebook);
        } else {
            notebookService.clearCategoryByCategoryIdAndUserId(category.getId(), userId);
        }

        categoryRepository.delete(category);
    }

    private String normalizeCategoryName(CategoryRequest request) {
        if (request == null || request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }

        return request.getName().trim();
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
