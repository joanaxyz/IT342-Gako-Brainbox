package edu.cit.gako.brainbox.ai.repository;

import edu.cit.gako.brainbox.ai.entity.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {

    List<AiConversation> findByUserIdAndNotebookUuidOrderByUpdatedAtDesc(Long userId, String notebookUuid);

    Optional<AiConversation> findByUuidAndUserId(String uuid, Long userId);

    void deleteByUuidAndUserId(String uuid, Long userId);
}
