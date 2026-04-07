package edu.cit.gako.brainbox.notebook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.cit.gako.brainbox.notebook.entity.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByUserId(Long userId);
    List<Quiz> findByNotebookId(Long notebookId);
    Optional<Quiz> findByUuid(String uuid);
}
