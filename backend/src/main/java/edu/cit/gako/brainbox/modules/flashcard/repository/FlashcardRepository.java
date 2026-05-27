package edu.cit.gako.brainbox.modules.flashcard.repository;

import edu.cit.gako.brainbox.modules.flashcard.entity.Flashcard;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    @EntityGraph(attributePaths = {"notebook", "cards"})
    List<Flashcard> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"notebook", "cards"})
    List<Flashcard> findByNotebookId(Long notebookId);

    @EntityGraph(attributePaths = {"notebook", "cards"})
    Optional<Flashcard> findByUuid(String uuid);
}
