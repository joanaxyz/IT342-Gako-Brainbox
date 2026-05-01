package edu.cit.gako.brainbox.modules.quiz.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    @Column(columnDefinition = "TEXT")
    private String text;

    @ElementCollection
    @CollectionTable(name = "quiz_question_options", joinColumns = @JoinColumn(name = "question_id"))
    @OrderColumn(name = "option_index")
    @Column(name = "option_text", columnDefinition = "TEXT")
    private List<String> options = new ArrayList<>();

    private int correctIndex;
}
