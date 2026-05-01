package edu.cit.gako.brainbox.modules.notebook.service;

import edu.cit.gako.brainbox.modules.notebook.dto.request.NotebookVersionRequest;
import edu.cit.gako.brainbox.modules.notebook.dto.response.NotebookFullResponse;
import edu.cit.gako.brainbox.modules.notebook.dto.response.NotebookVersionResponse;
import edu.cit.gako.brainbox.modules.notebook.service.NotebookService;
import edu.cit.gako.brainbox.modules.notebook.entity.Notebook;
import edu.cit.gako.brainbox.modules.notebook.entity.NotebookVersion;
import edu.cit.gako.brainbox.modules.notebook.repository.NotebookVersionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotebookVersionService {
    private final NotebookVersionRepository notebookVersionRepository;
    private final NotebookService notebookService;
    private final NotebookVersionSnapshotService notebookVersionSnapshotService;

    @Transactional(readOnly = true)
    public List<NotebookVersionResponse> getNotebookVersions(String notebookUuid, Long userId) {
        Notebook notebook = notebookService.getNotebookByUuidAndUserId(notebookUuid, userId);
        return getNotebookVersions(notebook);
    }

    @Transactional(readOnly = true)
    public List<NotebookVersionResponse> getNotebookVersions(String notebookUuid) {
        Notebook notebook = notebookService.getNotebookByUuid(notebookUuid);
        return getNotebookVersions(notebook);
    }

    @Transactional(readOnly = true)
    public NotebookVersionResponse getNotebookVersion(String notebookUuid, Long versionId, Long userId) {
        Notebook notebook = notebookService.getNotebookByUuidAndUserId(notebookUuid, userId);
        return getNotebookVersion(notebook, versionId);
    }

    @Transactional(readOnly = true)
    public NotebookVersionResponse getNotebookVersion(String notebookUuid, Long versionId) {
        Notebook notebook = notebookService.getNotebookByUuid(notebookUuid);
        return getNotebookVersion(notebook, versionId);
    }

    private List<NotebookVersionResponse> getNotebookVersions(Notebook notebook) {
        return notebookVersionRepository.findByNotebookIdOrderByVersionDesc(notebook.getId()).stream()
                .map(this::mapWithoutContent)
                .toList();
    }

    private NotebookVersionResponse getNotebookVersion(Notebook notebook, Long versionId) {
        NotebookVersion version = notebookVersionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Version not found"));

        if (version.getNotebook() == null || !version.getNotebook().getId().equals(notebook.getId())) {
            throw new RuntimeException("Version does not belong to this notebook");
        }

        return mapWithContent(version);
    }

    @Transactional
    public NotebookVersionResponse createNotebookVersion(String notebookUuid, NotebookVersionRequest request, Long userId) {
        Notebook notebook = notebookService.getNotebookByUuidAndUserId(notebookUuid, userId);
        String html = request != null && request.getContent() != null ? request.getContent() : notebook.getContent();
        return mapWithContent(notebookVersionSnapshotService.createSnapshotIfChanged(notebook, html));
    }

    @Transactional
    public NotebookFullResponse restoreNotebookVersion(String notebookUuid, Long versionId, Long userId) {
        Notebook notebook = notebookService.getNotebookByUuidAndUserId(notebookUuid, userId);
        NotebookVersion version = notebookVersionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Version not found"));

        if (version.getNotebook() == null || !version.getNotebook().getId().equals(notebook.getId())) {
            throw new RuntimeException("Version does not belong to this notebook");
        }

        notebookVersionSnapshotService.createSnapshotIfChanged(notebook, notebook.getContent());

        String restoredContent = notebookVersionSnapshotService.readContent(version.getContent());
        return notebookService.saveContent(notebookUuid, userId, restoredContent);
    }

    private NotebookVersionResponse mapWithContent(NotebookVersion version) {
        NotebookVersionResponse response = new NotebookVersionResponse();
        response.setId(version.getId());
        response.setContent(notebookVersionSnapshotService.readContent(version.getContent()));
        response.setVersion(version.getVersion());
        return response;
    }

    private NotebookVersionResponse mapWithoutContent(NotebookVersion version) {
        NotebookVersionResponse response = new NotebookVersionResponse();
        response.setId(version.getId());
        response.setVersion(version.getVersion());
        return response;
    }
}
