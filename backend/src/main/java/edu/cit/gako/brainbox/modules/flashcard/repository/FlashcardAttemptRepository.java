package edu.cit.gako.brainbox.modules.flashcard.repository;

import edu.cit.gako.brainbox.modules.flashcard.entity.FlashcardAttempt;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlashcardAttemptRepository extends JpaRepository<FlashcardAttempt, Long> {
    Optional<FlashcardAttempt> findByUserIdAndClientMutationId(Long userId, String clientMutationId);
    long countByFlashcardId(Long flashcardId);
    void deleteByFlashcardId(Long flashcardId);

    @Query("SELECT MAX(a.mastery) FROM FlashcardAttempt a WHERE a.flashcard.id = :flashcardId")
    Optional<Integer> findBestMasteryByFlashcardId(@Param("flashcardId") Long flashcardId);
}
