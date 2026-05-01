package edu.cit.gako.brainbox.modules.quiz.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizAttemptRequest {
    private int score;
    private String clientMutationId;
}
