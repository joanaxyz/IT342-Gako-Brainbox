package edu.cit.gako.brainbox.notebook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.cit.gako.brainbox.notebook.entity.QuizAttempt;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByQuizId(Long quizId);
    Optional<QuizAttempt> findByUserIdAndClientMutationId(Long userId, String clientMutationId);
    long countByQuizId(Long quizId);
    void deleteByQuizId(Long quizId);

    @Query("SELECT MAX(a.score) FROM QuizAttempt a WHERE a.quiz.id = :quizId")
    Optional<Integer> findBestScoreByQuizId(@Param("quizId") Long quizId);
}
