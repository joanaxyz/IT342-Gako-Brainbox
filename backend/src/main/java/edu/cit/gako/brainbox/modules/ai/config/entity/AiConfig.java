package edu.cit.gako.brainbox.modules.ai.config.entity;

import edu.cit.gako.brainbox.modules.user.entity.User;
import edu.cit.gako.brainbox.platform.security.interfaces.UserOwned;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "ai_config")
public class AiConfig implements UserOwned {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String proxyUrl;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String apiKey;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
    }
}
