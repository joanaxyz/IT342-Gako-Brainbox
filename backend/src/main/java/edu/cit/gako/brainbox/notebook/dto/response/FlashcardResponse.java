package edu.cit.gako.brainbox.notebook.dto.response;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlashcardResponse {
    private String uuid;
    private String title;
    private String description;
    private String notebookUuid;
    private String notebookTitle;
    private int cardCount;
    private Integer bestMastery;
    private long attempts;
    private List<FlashcardCardResponse> cards;
    private Instant createdAt;
    private Instant updatedAt;
}
