package edu.cit.gako.brainbox.modules.quiz.repository;

import edu.cit.gako.brainbox.modules.quiz.entity.Quiz;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    @EntityGraph(attributePaths = {"notebook", "questions", "questions.options"})
    List<Quiz> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"notebook", "questions", "questions.options"})
    List<Quiz> findByNotebookId(Long notebookId);

    @EntityGraph(attributePaths = {"notebook", "questions", "questions.options"})
    Optional<Quiz> findByUuid(String uuid);
}
