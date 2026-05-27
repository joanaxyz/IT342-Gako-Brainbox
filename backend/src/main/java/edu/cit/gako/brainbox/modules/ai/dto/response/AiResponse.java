package edu.cit.gako.brainbox.modules.ai.dto.response;

import edu.cit.gako.brainbox.modules.ai.dto.AiSelectionEdit;
import edu.cit.gako.brainbox.modules.ai.dto.AiEditorCommand;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AiResponse {
    private String response;
    private String action;
    private String editorContent;
    private String conversationTitle;
    private List<AiSelectionEdit> selectionEdits;
    private List<AiEditorCommand> editorCommands;
    private Object quizData;
    private Object flashcardData;

    public AiResponse(String response) {
        this.response = response;
        this.action = "none";
        this.editorContent = null;
        this.conversationTitle = null;
        this.selectionEdits = null;
        this.editorCommands = null;
    }

    public AiResponse(String response, String action, String editorContent, String conversationTitle) {
        this.response = response;
        this.action = action;
        this.editorContent = editorContent;
        this.conversationTitle = conversationTitle;
        this.selectionEdits = null;
        this.editorCommands = null;
    }

    public AiResponse(
        String response,
        String action,
        String editorContent,
        String conversationTitle,
        List<AiSelectionEdit> selectionEdits,
        List<AiEditorCommand> editorCommands,
        Object quizData,
        Object flashcardData
    ) {
        this.response = response;
        this.action = action;
        this.editorContent = editorContent;
        this.conversationTitle = conversationTitle;
        this.selectionEdits = selectionEdits;
        this.editorCommands = editorCommands;
        this.quizData = quizData;
        this.flashcardData = flashcardData;
    }
}
