package edu.cit.gako.brainbox.notebook.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.gako.brainbox.auth.annotation.RequireAuth;
import edu.cit.gako.brainbox.common.dto.ApiResponse;
import edu.cit.gako.brainbox.notebook.dto.request.NotebookVersionRequest;
import edu.cit.gako.brainbox.notebook.dto.response.NotebookFullResponse;
import edu.cit.gako.brainbox.notebook.dto.response.NotebookVersionResponse;
import edu.cit.gako.brainbox.notebook.service.NotebookVersionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/notebooks")
@RequiredArgsConstructor
public class NotebookVersionController {
    private final NotebookVersionService notebookVersionService;

    @RequireAuth
    @GetMapping("/{notebookUuid}/versions")
    public ResponseEntity<ApiResponse<List<NotebookVersionResponse>>> getNotebookVersions(@PathVariable String notebookUuid, @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(notebookVersionService.getNotebookVersions(notebookUuid, userId)));
    }

    @RequireAuth
    @GetMapping("/{notebookUuid}/versions/{versionId}")
    public ResponseEntity<ApiResponse<NotebookVersionResponse>> getNotebookVersion(@PathVariable String notebookUuid, @PathVariable Long versionId, @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(notebookVersionService.getNotebookVersion(notebookUuid, versionId, userId)));
    }

    @RequireAuth
    @PostMapping("/{notebookUuid}/versions")
    public ResponseEntity<ApiResponse<NotebookVersionResponse>> createNotebookVersion(@PathVariable String notebookUuid, @RequestBody NotebookVersionRequest request, @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(notebookVersionService.createNotebookVersion(notebookUuid, request, userId)));
    }

    @RequireAuth
    @PostMapping("/{notebookUuid}/versions/{versionId}/restore")
    public ResponseEntity<ApiResponse<NotebookFullResponse>> restoreNotebookVersion(
            @PathVariable String notebookUuid,
            @PathVariable Long versionId,
            @RequestAttribute Long userId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                notebookVersionService.restoreNotebookVersion(notebookUuid, versionId, userId)
        ));
    }
    
}
