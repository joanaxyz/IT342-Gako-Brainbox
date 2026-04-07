package edu.cit.gako.brainbox.notebook.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.cit.gako.brainbox.notebook.entity.NotebookVersion;

public interface NotebookVersionRepository extends JpaRepository<NotebookVersion, Long> {
    List<NotebookVersion> findByNotebookIdOrderByVersionDesc(Long notebookId);
    Optional<NotebookVersion> findTopByNotebookIdOrderByVersionDesc(Long notebookId);
    void deleteByNotebookId(Long notebookId);
}
