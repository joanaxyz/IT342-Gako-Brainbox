package edu.cit.gako.brainbox.modules.notebook.controller;

import edu.cit.gako.brainbox.modules.notebook.dto.response.NotebookFullResponse;
import edu.cit.gako.brainbox.modules.notebook.service.NotebookService;
import edu.cit.gako.brainbox.modules.user.entity.UserRole;
import edu.cit.gako.brainbox.platform.security.annotation.RequireRole;
import edu.cit.gako.brainbox.shared.controller.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/notebooks")
@RequiredArgsConstructor
public class AdminNotebookController {

    private final NotebookService notebookService;

    @RequireRole(UserRole.ADMIN)
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotebookFullResponse>>> getNotebooks() {
        return ResponseEntity.ok(ApiResponse.success(notebookService.getAllFullNotebooks()));
    }

    @RequireRole(UserRole.ADMIN)
    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<NotebookFullResponse>> getNotebook(@PathVariable String uuid) {
        return ResponseEntity.ok(ApiResponse.success(notebookService.getFullNotebookResponseByUuid(uuid)));
    }

    @RequireRole(UserRole.ADMIN)
    @DeleteMapping("/{uuid}")
    public ResponseEntity<ApiResponse<Void>> deleteNotebook(@PathVariable String uuid) {
        notebookService.deleteNotebook(uuid);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
