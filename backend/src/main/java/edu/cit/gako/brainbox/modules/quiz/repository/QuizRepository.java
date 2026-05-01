package edu.cit.gako.brainbox.modules.quiz.repository;

import edu.cit.gako.brainbox.modules.quiz.entity.Quiz;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByUserId(Long userId);
    List<Quiz> findByNotebookId(Long notebookId);
    Optional<Quiz> findByUuid(String uuid);
}
