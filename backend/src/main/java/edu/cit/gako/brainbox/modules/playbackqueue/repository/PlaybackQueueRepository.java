package edu.cit.gako.brainbox.modules.playbackqueue.repository;

import edu.cit.gako.brainbox.modules.playbackqueue.entity.PlaybackQueue;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaybackQueueRepository extends JpaRepository<PlaybackQueue, Long> {
    Optional<PlaybackQueue> findByUserId(Long userId);
}
