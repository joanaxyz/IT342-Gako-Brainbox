package edu.cit.gako.brainbox.notebook.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.cit.gako.brainbox.notebook.entity.Playlist;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUserId(Long userId);
    List<Playlist> findDistinctByQueueId(Long notebookId);
    Optional<Playlist> findByUuid(String uuid);
}
