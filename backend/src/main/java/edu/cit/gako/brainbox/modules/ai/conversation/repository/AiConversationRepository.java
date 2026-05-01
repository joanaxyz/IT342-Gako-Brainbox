package edu.cit.gako.brainbox.modules.ai.conversation.repository;

import edu.cit.gako.brainbox.modules.ai.conversation.entity.AiConversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {

    List<AiConversation> findByUserIdAndNotebookUuidOrderByUpdatedAtDesc(Long userId, String notebookUuid);

    Optional<AiConversation> findByUuidAndUserId(String uuid, Long userId);

    Optional<AiConversation> findByUuid(String uuid);

    void deleteByUuidAndUserId(String uuid, Long userId);
}
