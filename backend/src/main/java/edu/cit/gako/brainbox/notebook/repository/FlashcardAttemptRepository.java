package edu.cit.gako.brainbox.notebook.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.cit.gako.brainbox.notebook.entity.FlashcardAttempt;

public interface FlashcardAttemptRepository extends JpaRepository<FlashcardAttempt, Long> {
    long countByFlashcardId(Long flashcardId);
    void deleteByFlashcardId(Long flashcardId);

    @Query("SELECT MAX(a.mastery) FROM FlashcardAttempt a WHERE a.flashcard.id = :flashcardId")
    Optional<Integer> findBestMasteryByFlashcardId(@Param("flashcardId") Long flashcardId);
}
