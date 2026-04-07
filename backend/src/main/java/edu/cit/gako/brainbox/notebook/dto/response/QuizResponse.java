package edu.cit.gako.brainbox.notebook.dto.response;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizResponse {
    private String uuid;
    private String title;
    private String description;
    private String difficulty;
    private String notebookUuid;
    private String notebookTitle;
    private int questionCount;
    private String estimatedTime;
    private Integer bestScore;
    private long attempts;
    private List<QuizQuestionResponse> questions;
    private Instant createdAt;
    private Instant updatedAt;
}
