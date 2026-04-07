package edu.cit.gako.brainbox.ai.dto.request;

import lombok.Data;
import java.util.List;
import java.util.Map;

import edu.cit.gako.brainbox.ai.dto.AiSelectionTarget;

@Data
public class AiRequest {
    private String query;
    private String notebookUuid;
    private List<Map<String, String>> conversationHistory;
    private String selectedText;
    private List<AiSelectionTarget> aiSelections;
    private String selectionMode;
    private String mode;
}