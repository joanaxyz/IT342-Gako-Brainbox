package edu.cit.gako.brainbox.modules.playlist.repository;

import edu.cit.gako.brainbox.modules.playlist.entity.Playlist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUserId(Long userId);
    List<Playlist> findDistinctByQueueId(Long notebookId);
    Optional<Playlist> findByUuid(String uuid);
}
