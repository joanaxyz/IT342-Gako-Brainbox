package edu.cit.gako.brainbox.ai.prompt;

import edu.cit.gako.brainbox.ai.dto.AiSelectionTarget;

import java.util.List;

/**
 * Template Method Pattern — concrete subclass for review mode.
 * Overrides buildModeBlock() to restrict actions to read-only operations only.
 */
public class ReviewModePromptBuilder extends AiPromptBuilder {

    public ReviewModePromptBuilder(String notebookTitle, String context,
                                   String selectedText, String selectionMode,
                                   List<AiSelectionTarget> aiSelections) {
        super(notebookTitle, context, selectedText, selectionMode, aiSelections);
    }

    @Override
    protected String buildModeBlock() {
        return "---\nASSISTANT MODE: review\n\nYou are inside BrainBox review mode.\n"
            + "- Allowed actions are ONLY: \"none\", \"create_quiz\", and \"create_flashcard\".\n"
            + "- Never propose editor mutations, replacements, or appended content.\n"
            + "- If the user asks to edit, rewrite, or insert content, reply helpfully but keep action as \"none\" and explain that review mode is read-only.\n\n";
    }
}
