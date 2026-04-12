package edu.cit.gako.brainbox.notebook.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
        name = "notebook_mutation_record",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "client_mutation_id"})
)
public class NotebookMutationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "notebook_uuid")
    private String notebookUuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "mutation_type", nullable = false)
    private NotebookMutationType mutationType;

    @Column(name = "client_mutation_id", nullable = false)
    private String clientMutationId;

    @Column(name = "response_json", columnDefinition = "TEXT")
    private String responseJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
