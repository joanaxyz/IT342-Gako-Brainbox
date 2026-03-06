package com.example.brainbox_api.notebook.service;

import org.springframework.stereotype.Service;

import com.example.brainbox_api.auth.entity.User;
import com.example.brainbox_api.auth.service.UserService;
import com.example.brainbox_api.notebook.repository.NotebookRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import com.example.brainbox_api.notebook.entity.Notebook;
import com.example.brainbox_api.notebook.entity.Category;
import com.example.brainbox_api.notebook.dto.request.NotebookRequest;
import com.example.brainbox_api.notebook.dto.response.NotebookFullResponse;
import com.example.brainbox_api.notebook.dto.response.NotebookOverviewResponse;
import com.example.brainbox_api.notebook.repository.CategoryRepository;

@Service
@RequiredArgsConstructor
public class NotebookService {
    private final NotebookRepository notebookRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public NotebookFullResponse createNotebook(NotebookRequest request, Long userId) {
        Notebook notebook = new Notebook();
        notebook.setTitle(request.getTitle());
        notebook.setContent(request.getContent());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            notebook.setCategory(category);
        }

        notebook.setUser(userService.findById(userId));
        return mapToFullResponse(notebookRepository.save(notebook));
    }

    // intended for admins
    public List<NotebookFullResponse> getAllFullNotebooks() {
        return notebookRepository.findAll().stream()
                .map(this::mapToFullResponse)
                .toList();
    }

    public List<NotebookFullResponse> getFullNotebooksByUser(Long id) {
        return notebookRepository.findAll().stream()
                .filter(d -> d.getUser().getId().equals(id))
                .map(this::mapToFullResponse)
                .toList();
    }

    public NotebookFullResponse getFullNotebookResponseById(Long id) {
        return mapToFullResponse(getNotebookById(id));
    }

    public List<NotebookOverviewResponse> getNotebookOverviewsByUser(Long id) {
        return notebookRepository.findAll().stream()   
                .filter(d -> d.getUser().getId().equals(id))
                .map(this::mapToOverviewResponse)
                .toList();
    }

    public Notebook getNotebookById(Long id) {
        return notebookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notebook not found"));
    }

    public NotebookFullResponse updateNotebook(Long id, NotebookRequest request) {
        Notebook notebook = getNotebookById(id);
        if (request.getTitle() != null) {
            notebook.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            notebook.setContent(request.getContent());
        }
        
        // Handle category update
        if (request.getCategoryId() != null) {
            if (request.getCategoryId() == -1) {
                // Special value to set category to null (uncategorized)
                notebook.setCategory(null);
            } else {
                Category category = categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                notebook.setCategory(category);
            }
        }
        return mapToFullResponse(notebookRepository.save(notebook));
    }

    public void deleteNotebook(Long id) {
        notebookRepository.deleteById(id);
    }

    private NotebookFullResponse mapToFullResponse(Notebook notebook) {
        NotebookFullResponse response = new NotebookFullResponse();
        response.setId(notebook.getId());
        response.setTitle(notebook.getTitle());
        response.setContent(notebook.getContent());
        response.setCreatedAt(notebook.getCreatedAt());
        response.setUpdatedAt(notebook.getUpdatedAt());
        response.setLastReviewedAt(notebook.getLastReviewedAt());
        if (notebook.getCategory() != null) {
            response.setCategoryId(notebook.getCategory().getId());
            response.setCategoryName(notebook.getCategory().getName());
        }
        return response;
    }

    private NotebookOverviewResponse mapToOverviewResponse(Notebook notebook) {
        NotebookOverviewResponse response = new NotebookOverviewResponse();
        response.setId(notebook.getId());
        response.setTitle(notebook.getTitle());
        response.setCreatedAt(notebook.getCreatedAt());
        response.setUpdatedAt(notebook.getUpdatedAt());
        response.setLastReviewedAt(notebook.getLastReviewedAt());
        if (notebook.getCategory() != null) {
            response.setCategoryId(notebook.getCategory().getId());
            response.setCategoryName(notebook.getCategory().getName());
        }
        return response;
    }
}
