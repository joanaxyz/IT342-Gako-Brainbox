package edu.cit.gako.brainbox.ai.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import edu.cit.gako.brainbox.ai.dto.AiSelectionEdit;

@Data
@NoArgsConstructor
public class AiResponse {
    private String response;
    private String action;
    private String editorContent;
    private String conversationTitle;
    private List<AiSelectionEdit> selectionEdits;
    private Object quizData;
    private Object flashcardData;

    public AiResponse(String response) {
        this.response = response;
        this.action = "none";
        this.editorContent = null;
        this.conversationTitle = null;
        this.selectionEdits = null;
    }

    public AiResponse(String response, String action, String editorContent, String conversationTitle) {
        this.response = response;
        this.action = action;
        this.editorContent = editorContent;
        this.conversationTitle = conversationTitle;
        this.selectionEdits = null;
    }

    public AiResponse(
        String response,
        String action,
        String editorContent,
        String conversationTitle,
        List<AiSelectionEdit> selectionEdits,
        Object quizData,
        Object flashcardData
    ) {
        this.response = response;
        this.action = action;
        this.editorContent = editorContent;
        this.conversationTitle = conversationTitle;
        this.selectionEdits = selectionEdits;
        this.quizData = quizData;
        this.flashcardData = flashcardData;
    }
}
