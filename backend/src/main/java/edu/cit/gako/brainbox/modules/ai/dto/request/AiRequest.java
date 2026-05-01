package edu.cit.gako.brainbox.modules.ai.dto.request;

import edu.cit.gako.brainbox.modules.ai.dto.AiSelectionTarget;
import java.util.List;
import java.util.Map;
import lombok.Data;

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