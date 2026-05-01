package edu.cit.gako.brainbox.modules.offlinebundle.controller;

import edu.cit.gako.brainbox.modules.offlinebundle.dto.request.OfflineNotebookBundleRequest;
import edu.cit.gako.brainbox.modules.offlinebundle.dto.response.OfflineNotebookBundleResponse;
import edu.cit.gako.brainbox.modules.offlinebundle.service.OfflineNotebookBundleService;
import edu.cit.gako.brainbox.platform.security.annotation.RequireAuth;
import edu.cit.gako.brainbox.shared.controller.ApiResponse;
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

    private final OfflineNotebookBundleService offlineNotebookBundleService;

    @RequireAuth
    @PostMapping("/notebooks")
    public ResponseEntity<ApiResponse<OfflineNotebookBundleResponse>> getNotebookBundles(
            @RequestBody OfflineNotebookBundleRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                offlineNotebookBundleService.getNotebookBundle(request, userId)));
    }
}
