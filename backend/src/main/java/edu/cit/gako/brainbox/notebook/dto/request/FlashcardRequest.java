package edu.cit.gako.brainbox.notebook.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlashcardRequest {
    private String title;
    private String description;
    private String notebookUuid;
    private List<FlashcardCardRequest> cards;
}
