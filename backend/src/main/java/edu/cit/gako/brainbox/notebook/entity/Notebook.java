package edu.cit.gako.brainbox.notebook.entity;

import java.time.Instant;

import edu.cit.gako.brainbox.auth.entity.User;
import edu.cit.gako.brainbox.auth.interfaces.UserOwned;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Notebook implements UserOwned {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String uuid;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant lastReviewedAt;

    private Long version;

    @ManyToOne
    @JoinColumn(name="category_id", nullable=true)
    private Category category;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    private void prePersist(){
        if(uuid == null) uuid = UUID.randomUUID().toString();
        if(createdAt == null) createdAt = Instant.now();
        if(updatedAt == null) updatedAt = Instant.now();
        if(version == null) version = 0L;
    }

    @PreUpdate
    private void preUpdate(){
        updatedAt = Instant.now();
        version = version == null ? 1L : version + 1;
    }
}
