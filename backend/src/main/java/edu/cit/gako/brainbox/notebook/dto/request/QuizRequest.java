package edu.cit.gako.brainbox.notebook.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizRequest {
    private String title;
    private String description;
    private String difficulty;
    private String notebookUuid;
    private List<QuizQuestionRequest> questions;
}
