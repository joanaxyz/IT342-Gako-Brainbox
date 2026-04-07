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
public class Flashcard implements UserOwned {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String uuid;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(optional = true)
    @JoinColumn(name = "notebook_id", nullable = true)
    private Notebook notebook;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "flashcard_id")
    @OrderColumn(name = "position")
    private List<FlashcardCard> cards = new ArrayList<>();

    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    private void prePersist() {
        if (uuid == null) uuid = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
    }
}
