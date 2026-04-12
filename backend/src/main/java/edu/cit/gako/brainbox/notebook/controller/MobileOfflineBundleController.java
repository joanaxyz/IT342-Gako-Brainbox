package edu.cit.gako.brainbox.notebook.controller;

import edu.cit.gako.brainbox.auth.annotation.RequireAuth;
import edu.cit.gako.brainbox.common.dto.ApiResponse;
import edu.cit.gako.brainbox.notebook.dto.request.OfflineNotebookBundleRequest;
import edu.cit.gako.brainbox.notebook.dto.response.OfflineNotebookBundleResponse;
import edu.cit.gako.brainbox.notebook.service.NotebookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile/offline-bundles")
@RequiredArgsConstructor
public class MobileOfflineBundleController {

    private final NotebookService notebookService;

    @RequireAuth
    @PostMapping("/notebooks")
    public ResponseEntity<ApiResponse<OfflineNotebookBundleResponse>> getNotebookBundles(
            @RequestBody OfflineNotebookBundleRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                notebookService.getOfflineNotebookBundle(request, userId)));
    }
}
