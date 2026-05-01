package edu.cit.gako.brainbox.modules.flashcard.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlashcardAttemptRequest {
    private int mastery;
    private String clientMutationId;
}
