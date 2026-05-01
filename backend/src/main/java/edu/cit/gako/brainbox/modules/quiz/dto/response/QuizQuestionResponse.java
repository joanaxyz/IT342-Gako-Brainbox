package edu.cit.gako.brainbox.modules.quiz.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizQuestionResponse {
    private String type;
    private String text;
    private List<String> options;
    private int correctIndex;
}
