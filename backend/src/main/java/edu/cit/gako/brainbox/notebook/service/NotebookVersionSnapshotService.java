package edu.cit.gako.brainbox.notebook.service;

import edu.cit.gako.brainbox.notebook.entity.Notebook;
import edu.cit.gako.brainbox.notebook.entity.NotebookVersion;
import edu.cit.gako.brainbox.notebook.repository.NotebookVersionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotebookVersionSnapshotService {
    private static final String SNAPSHOT_FORMAT = "content-v1";
    private static final String LEGACY_FORMAT = "page-snapshot-v1";

    private final NotebookVersionRepository notebookVersionRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public NotebookVersion createSnapshot(Notebook notebook, String html) {
        NotebookVersion notebookVersion = new NotebookVersion();
        notebookVersion.setNotebook(notebook);
        notebookVersion.setContent(writeSnapshot(html != null ? html : ""));
        return notebookVersionRepository.save(notebookVersion);
    }

    @Transactional
    public NotebookVersion createSnapshotIfChanged(Notebook notebook, String html) {
        String normalizedHtml = html != null ? html : "";
        NotebookVersion latestVersion = notebookVersionRepository
                .findTopByNotebookIdOrderByVersionDesc(notebook.getId())
                .orElse(null);

        if (latestVersion != null && normalizedHtml.equals(readContent(latestVersion.getContent()))) {
            return latestVersion;
        }

        return createSnapshot(notebook, normalizedHtml);
    }

    public String readContent(String stored) {
        if (stored == null || stored.isBlank()) {
            return "";
        }

        try {
            ContentSnapshot snapshot = objectMapper.readValue(stored, ContentSnapshot.class);
            if (SNAPSHOT_FORMAT.equals(snapshot.getFormat()) && snapshot.getContent() != null) {
                return snapshot.getContent();
            }

            if (LEGACY_FORMAT.equals(snapshot.getFormat())) {
                LegacySnapshot legacy = objectMapper.readValue(stored, LegacySnapshot.class);
                if (legacy.getPreviewHtml() != null) {
                    return legacy.getPreviewHtml();
                }
            }
        } catch (Exception ignored) {
            // Stored value may already be raw HTML from an older format.
        }

        return stored;
    }

    private String writeSnapshot(String html) {
        ContentSnapshot snapshot = new ContentSnapshot();
        snapshot.setFormat(SNAPSHOT_FORMAT);
        snapshot.setContent(html);

        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize notebook version snapshot", e);
        }
    }

    @Data
    private static class ContentSnapshot {
        private String format;
        private String content;
    }

    @Data
    private static class LegacySnapshot {
        private String format;
        private String previewHtml;
    }
}
