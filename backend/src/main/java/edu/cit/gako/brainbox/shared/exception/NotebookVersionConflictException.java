package edu.cit.gako.brainbox.shared.exception;

import edu.cit.gako.brainbox.modules.notebook.dto.response.NotebookFullResponse;

public class NotebookVersionConflictException extends RuntimeException {
    private final NotebookFullResponse latestNotebook;

    public NotebookVersionConflictException(String message, NotebookFullResponse latestNotebook) {
        super(message);
        this.latestNotebook = latestNotebook;
    }

    public NotebookFullResponse getLatestNotebook() {
        return latestNotebook;
    }
}
