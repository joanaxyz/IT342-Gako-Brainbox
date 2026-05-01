package edu.cit.gako.brainbox.modules.notebook.repository;

import edu.cit.gako.brainbox.modules.notebook.entity.NotebookMutationRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotebookMutationRecordRepository extends JpaRepository<NotebookMutationRecord, Long> {
    Optional<NotebookMutationRecord> findByUserIdAndClientMutationId(Long userId, String clientMutationId);
}
