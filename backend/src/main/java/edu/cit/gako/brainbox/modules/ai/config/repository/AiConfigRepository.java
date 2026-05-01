package edu.cit.gako.brainbox.modules.ai.config.repository;

import edu.cit.gako.brainbox.modules.ai.config.entity.AiConfig;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiConfigRepository extends JpaRepository<AiConfig, Long> {
    List<AiConfig> findByUser_IdOrderByUpdatedAtDesc(Long userId);

    Optional<AiConfig> findByIdAndUser_Id(Long id, Long userId);
}
