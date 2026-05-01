package edu.cit.gako.brainbox.modules.notebook.repository;

import edu.cit.gako.brainbox.modules.notebook.entity.NotebookVersion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotebookVersionRepository extends JpaRepository<NotebookVersion, Long> {
    List<NotebookVersion> findByNotebookIdOrderByVersionDesc(Long notebookId);
    Optional<NotebookVersion> findTopByNotebookIdOrderByVersionDesc(Long notebookId);
    void deleteByNotebookId(Long notebookId);
}
