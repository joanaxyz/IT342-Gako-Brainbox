package edu.cit.gako.brainbox.notebook.event;

import edu.cit.gako.brainbox.notebook.entity.Notebook;
import org.springframework.context.ApplicationEvent;

/**
 * Observer Pattern — domain event published whenever notebook content is persisted.
 *
 * NotebookService (the Subject) publishes this event via ApplicationEventPublisher.
 * Any Spring bean can subscribe by implementing ApplicationListener<NotebookContentSavedEvent>
 * or using @EventListener, without NotebookService needing a direct dependency on each observer.
 *
 * Current observer: NotebookVersionSnapshotListener (creates a version snapshot).
 */
public class NotebookContentSavedEvent extends ApplicationEvent {

    private final Notebook notebook;
    private final String content;

    public NotebookContentSavedEvent(Object source, Notebook notebook, String content) {
        super(source);
        this.notebook = notebook;
        this.content = content;
    }

    public Notebook getNotebook() {
        return notebook;
    }

    public String getContent() {
        return content;
    }
}
