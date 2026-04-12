package edu.cit.gako.brainbox.notebook.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.cit.gako.brainbox.notebook.entity.NotebookMutationRecord;

public interface NotebookMutationRecordRepository extends JpaRepository<NotebookMutationRecord, Long> {
    Optional<NotebookMutationRecord> findByUserIdAndClientMutationId(Long userId, String clientMutationId);
}
