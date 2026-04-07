package edu.cit.gako.brainbox.notebook.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Column;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class NotebookVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Instant version;

    @ManyToOne
    @JoinColumn(name="notebook_id", nullable=true)
    private Notebook notebook;

    @PrePersist
    private void prePersist(){
        if(version == null) version = Instant.now();
    }
}