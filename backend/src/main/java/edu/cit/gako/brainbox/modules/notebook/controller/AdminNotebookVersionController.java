package edu.cit.gako.brainbox.modules.notebook.controller;

import edu.cit.gako.brainbox.modules.notebook.dto.response.NotebookVersionResponse;
import edu.cit.gako.brainbox.modules.notebook.service.NotebookVersionService;
import edu.cit.gako.brainbox.modules.user.entity.UserRole;
import edu.cit.gako.brainbox.platform.security.annotation.RequireRole;
import edu.cit.gako.brainbox.shared.controller.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/notebooks/{notebookUuid}/versions")
@RequiredArgsConstructor
public class AdminNotebookVersionController {

    private final NotebookVersionService notebookVersionService;

    @RequireRole(UserRole.ADMIN)
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotebookVersionResponse>>> getVersions(@PathVariable String notebookUuid) {
        return ResponseEntity.ok(ApiResponse.success(notebookVersionService.getNotebookVersions(notebookUuid)));
    }

    @RequireRole(UserRole.ADMIN)
    @GetMapping("/{versionId}")
    public ResponseEntity<ApiResponse<NotebookVersionResponse>> getVersion(
            @PathVariable String notebookUuid,
            @PathVariable Long versionId) {
        return ResponseEntity.ok(ApiResponse.success(notebookVersionService.getNotebookVersion(notebookUuid, versionId)));
    }
}
