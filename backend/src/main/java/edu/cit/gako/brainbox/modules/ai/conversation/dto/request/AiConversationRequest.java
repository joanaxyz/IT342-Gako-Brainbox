package edu.cit.gako.brainbox.modules.ai.conversation.dto.request;

import lombok.Data;

@Data
public class AiConversationRequest {
    private String notebookUuid;
    private String mode;
    private String title;
    private String messages; // JSON string of message array
}
