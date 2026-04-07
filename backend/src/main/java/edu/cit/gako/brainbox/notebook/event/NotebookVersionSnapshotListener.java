package edu.cit.gako.brainbox.notebook.event;

import edu.cit.gako.brainbox.notebook.service.NotebookVersionSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern — concrete observer that reacts to NotebookContentSavedEvent
 * by creating a version snapshot.
 *
 * Previously NotebookService called NotebookVersionSnapshotService directly,
 * coupling the save logic to the versioning logic. Now NotebookService only
 * publishes an event; versioning is a separate concern handled here.
 */
@Component
@RequiredArgsConstructor
public class NotebookVersionSnapshotListener {

    private final NotebookVersionSnapshotService notebookVersionSnapshotService;

    @EventListener
    public void onNotebookContentSaved(NotebookContentSavedEvent event) {
        notebookVersionSnapshotService.createSnapshot(event.getNotebook(), event.getContent());
    }
}
