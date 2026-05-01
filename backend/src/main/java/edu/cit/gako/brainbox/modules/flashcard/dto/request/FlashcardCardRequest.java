package edu.cit.gako.brainbox.modules.flashcard.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlashcardCardRequest {
    private String front;
    private String back;
}
