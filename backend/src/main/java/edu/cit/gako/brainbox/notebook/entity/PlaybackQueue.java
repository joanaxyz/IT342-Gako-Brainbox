package edu.cit.gako.brainbox.notebook.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import edu.cit.gako.brainbox.auth.entity.User;
import edu.cit.gako.brainbox.auth.interfaces.UserOwned;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PlaybackQueue implements UserOwned {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String uuid;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToMany
    @JoinTable(
        name = "playback_queue_notebooks",
        joinColumns = @JoinColumn(name = "queue_id"),
        inverseJoinColumns = @JoinColumn(name = "notebook_id")
    )
    @OrderColumn(name = "position")
    private List<Notebook> items = new ArrayList<>();

    private Instant updatedAt;

    @PrePersist
    private void prePersist() {
        if (uuid == null) uuid = UUID.randomUUID().toString();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
    }
}
