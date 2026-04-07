package edu.cit.gako.brainbox.ai.prompt;

import edu.cit.gako.brainbox.ai.dto.AiSelectionTarget;

import java.util.List;

/**
 * Template Method Pattern — concrete subclass for editor mode.
 * Overrides buildModeBlock() to allow full editor actions.
 */
public class EditorModePromptBuilder extends AiPromptBuilder {

    public EditorModePromptBuilder(String notebookTitle, String context,
                                   String selectedText, String selectionMode,
                                   List<AiSelectionTarget> aiSelections) {
        super(notebookTitle, context, selectedText, selectionMode, aiSelections);
    }

    @Override
    protected String buildModeBlock() {
        return "---\nASSISTANT MODE: editor\n\nYou may use editor actions when the user clearly wants notebook content changed.\n\n";
    }
}
