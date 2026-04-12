package edu.cit.gako.brainbox.notebook.controller;

import edu.cit.gako.brainbox.common.dto.ApiResponse;
import edu.cit.gako.brainbox.notebook.service.NotebookService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.cit.gako.brainbox.auth.annotation.RequireAuth;
import edu.cit.gako.brainbox.notebook.dto.request.NotebookMutationRequest;
import edu.cit.gako.brainbox.notebook.dto.request.NotebookContentRequest;
import edu.cit.gako.brainbox.notebook.dto.request.NotebookRequest;
import edu.cit.gako.brainbox.notebook.dto.response.NotebookFullResponse;
import edu.cit.gako.brainbox.notebook.dto.response.NotebookOverviewResponse;
import org.springframework.web.bind.annotation.GetMapping;

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
    @GetMapping("/recently-edited")
    public ResponseEntity<ApiResponse<List<NotebookOverviewResponse>>> getRecentlyEdited(@RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(notebookService.getRecentlyEditedNotebooksByUser(userId)));
    }

    @RequireAuth
    @GetMapping("/recently-reviewed")
    public ResponseEntity<ApiResponse<List<NotebookOverviewResponse>>> getRecentlyReviewed(@RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(notebookService.getRecentlyReviewedNotebooksByUser(userId)));
    }

    @RequireAuth
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<NotebookFullResponse>> getNotebook(@PathVariable("uuid") String notebookUuid, @RequestAttribute Long userId){
        return ResponseEntity.ok(ApiResponse.success(notebookService.getFullNotebookResponseByUuid(notebookUuid, userId)));
    }

    @RequireAuth
    @PutMapping("/{uuid}")
    public ResponseEntity<ApiResponse<NotebookFullResponse>> updateNotebook(@PathVariable("uuid") String notebookUuid, @RequestBody NotebookRequest notebookRequest, @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(notebookService.updateNotebook(notebookUuid, userId, notebookRequest)));
    }

    @RequireAuth
    @PatchMapping("/update-review/{uuid}")
    public ResponseEntity<ApiResponse<Void>> updateReview(
            @PathVariable("uuid") String notebookUuid,
            @RequestBody(required = false) NotebookMutationRequest body,
            @RequestAttribute Long userId) {
        notebookService.markNotebookReviewed(
                notebookUuid,
                userId,
                body != null ? body.getBaseVersion() : null,
                body != null ? body.getClientMutationId() : null);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @RequireAuth
    @PutMapping("/{uuid}/content")
    public ResponseEntity<ApiResponse<NotebookFullResponse>> saveContent(
            @PathVariable("uuid") String notebookUuid,
            @RequestBody NotebookContentRequest body,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                notebookService.saveContent(notebookUuid, userId, body.getContent(), body.getBaseVersion(), body.getClientMutationId())
        ));
    }

    @RequireAuth
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteNotebook(
            @PathVariable("uuid") String notebookUuid,
            @RequestBody(required = false) NotebookMutationRequest body,
            @RequestAttribute Long userId) {
        notebookService.deleteNotebook(
                notebookUuid,
                userId,
                body != null ? body.getBaseVersion() : null,
                body != null ? body.getClientMutationId() : null);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
