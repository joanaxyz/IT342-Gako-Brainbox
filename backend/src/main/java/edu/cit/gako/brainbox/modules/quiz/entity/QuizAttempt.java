package edu.cit.gako.brainbox.modules.quiz.entity;

import edu.cit.gako.brainbox.modules.quiz.entity.Quiz;
import edu.cit.gako.brainbox.modules.user.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "client_mutation_id"}))
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private int score;

    @Column(name = "client_mutation_id")
    private String clientMutationId;

    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
