package edu.cit.gako.brainbox.notebook.repository;

import edu.cit.gako.brainbox.notebook.entity.PlaybackQueue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaybackQueueRepository extends JpaRepository<PlaybackQueue, Long> {
    Optional<PlaybackQueue> findByUserId(Long userId);
}
