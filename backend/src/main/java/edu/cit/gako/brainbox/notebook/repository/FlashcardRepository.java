package edu.cit.gako.brainbox.notebook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.cit.gako.brainbox.notebook.entity.Flashcard;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {
    List<Flashcard> findByUserId(Long userId);
    List<Flashcard> findByNotebookId(Long notebookId);
    Optional<Flashcard> findByUuid(String uuid);
}
