package edu.cit.gako.brainbox.notebook.dto.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotebookFullResponse {
    private String uuid;
    private String title;
    private String content;
    private Integer wordCount;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastReviewedAt;
    private Long version;
    private Long categoryId;
    private String categoryName;
}
