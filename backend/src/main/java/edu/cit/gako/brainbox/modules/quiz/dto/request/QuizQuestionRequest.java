package edu.cit.gako.brainbox.modules.quiz.dto.request;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizQuestionRequest {
    private String type;
    private String text;
    private List<String> options;
    private int correctIndex;
}
