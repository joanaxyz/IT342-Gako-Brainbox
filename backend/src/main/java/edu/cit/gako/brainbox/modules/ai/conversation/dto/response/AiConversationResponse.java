package edu.cit.gako.brainbox.modules.ai.conversation.dto.response;

import edu.cit.gako.brainbox.modules.ai.conversation.entity.AiConversation;
import java.time.Instant;
import lombok.Getter;

@Getter
public class AiConversationResponse {
    private final String uuid;
    private final String notebookUuid;
    private final String mode;
    private final String title;
    private final String messages;
    private final Instant createdAt;
    private final Instant updatedAt;

    public AiConversationResponse(AiConversation conv) {
        this.uuid = conv.getUuid();
        this.notebookUuid = conv.getNotebookUuid();
        this.mode = conv.getMode();
        this.title = conv.getTitle();
        this.messages = conv.getMessages();
        this.createdAt = conv.getCreatedAt();
        this.updatedAt = conv.getUpdatedAt();
    }
}
