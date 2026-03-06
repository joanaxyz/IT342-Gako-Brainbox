package com.example.brainbox_api.notebook.dto.response;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotebookOverviewResponse {
    private Long id;
    private String title;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastReviewedAt;
    private Long categoryId;
    private String categoryName;
}
