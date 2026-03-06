package com.example.brainbox_api.notebook.controller;

import com.example.brainbox_api.common.dto.ApiResponse;
import com.example.brainbox_api.notebook.service.NotebookService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.brainbox_api.auth.annotation.RequireAuth;
import com.example.brainbox_api.notebook.dto.request.NotebookRequest;
import com.example.brainbox_api.notebook.dto.response.NotebookFullResponse;
import com.example.brainbox_api.notebook.dto.response.NotebookOverviewResponse;

@RestController
@RequestMapping("/api/notebooks")
@RequiredArgsConstructor
public class NotebookController {
    private final NotebookService notebookService;

    @RequireAuth
    @PostMapping
    public ResponseEntity<ApiResponse<NotebookFullResponse>> createNotebook(@RequestBody NotebookRequest notebookRequest, @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(notebookService.createNotebook(notebookRequest, userId)));
    }
    @RequireAuth
    @GetMapping()
    public ResponseEntity<ApiResponse<List<NotebookOverviewResponse>>> getNotebookOverview(@RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(notebookService.getNotebookOverviewsByUser(userId)));
    }

    @RequireAuth
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotebookFullResponse>> getNotebook(@PathVariable Long id){
        return ResponseEntity.ok(ApiResponse.success(notebookService.getFullNotebookResponseById(id)));
    }

    @RequireAuth
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NotebookFullResponse>> updateNotebook(@PathVariable Long id, @RequestBody NotebookRequest notebookRequest) {
        return ResponseEntity.ok(ApiResponse.success(notebookService.updateNotebook(id, notebookRequest)));
    }

    @RequireAuth
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotebook(@PathVariable Long id) {
        notebookService.deleteNotebook(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
