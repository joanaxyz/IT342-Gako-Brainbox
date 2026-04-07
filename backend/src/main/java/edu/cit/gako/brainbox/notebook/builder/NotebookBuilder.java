package edu.cit.gako.brainbox.notebook.builder;

import edu.cit.gako.brainbox.auth.entity.User;
import edu.cit.gako.brainbox.notebook.entity.Category;
import edu.cit.gako.brainbox.notebook.entity.Notebook;

/**
 * Builder Pattern: constructs a Notebook entity step-by-step, keeping
 * NotebookService free of scattered setter calls and null-guard logic.
 *
 * Usage:
 *   Notebook nb = new NotebookBuilder()
 *       .title(request.getTitle())
 *       .content(request.getContent())
 *       .category(category)
 *       .owner(user)
 *       .build();
 */
public class NotebookBuilder {

    private String title;
    private String content = "";
    private Category category;
    private User owner;

    public NotebookBuilder title(String title) {
        this.title = title;
        return this;
    }

    public NotebookBuilder content(String content) {
        this.content = content != null ? content : "";
        return this;
    }

    public NotebookBuilder category(Category category) {
        this.category = category;
        return this;
    }

    public NotebookBuilder owner(User owner) {
        this.owner = owner;
        return this;
    }

    public Notebook build() {
        if (owner == null) {
            throw new IllegalStateException("NotebookBuilder: owner (user) must be set before calling build()");
        }
        Notebook notebook = new Notebook();
        notebook.setTitle(title);
        notebook.setContent(content);
        notebook.setCategory(category);
        notebook.setUser(owner);
        return notebook;
    }
}
