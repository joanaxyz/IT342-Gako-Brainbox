package edu.cit.gako.brainbox.ai.repository;

import edu.cit.gako.brainbox.ai.entity.AiConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiConfigRepository extends JpaRepository<AiConfig, Long> {
    List<AiConfig> findByUser_IdOrderByUpdatedAtDesc(Long userId);

    Optional<AiConfig> findByIdAndUser_Id(Long id, Long userId);
}
