package edu.cit.gako.brainbox.modules.flashcard.repository;

import edu.cit.gako.brainbox.modules.flashcard.entity.Flashcard;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    List<Flashcard> findByUserId(Long userId);
    List<Flashcard> findByNotebookId(Long notebookId);
    Optional<Flashcard> findByUuid(String uuid);
}
