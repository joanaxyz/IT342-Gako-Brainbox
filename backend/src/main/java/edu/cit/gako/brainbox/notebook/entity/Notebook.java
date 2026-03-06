package com.example.brainbox_api.notebook.entity;

import java.time.Instant;

import com.example.brainbox_api.auth.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Notebook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    private Instant createdAt;
    
    private Instant updatedAt;

    private Instant lastReviewedAt;

    @ManyToOne
    @JoinColumn(name="category_id", nullable=true)
    private Category category;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    private void prePersist(){
        if(createdAt == null) createdAt = Instant.now();
        if(updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    private void preUpdate(){
        updatedAt = Instant.now();
    }
}
