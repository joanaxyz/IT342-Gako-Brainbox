package com.example.brainbox_api.auth.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;
    private Instant createdAt;

    @ManyToOne (optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String userAgent;

    private String ipAddress;

    public boolean isExpired() {
        return expiryDate.isBefore(Instant.now());
    }

    @PrePersist
    private void prePersist(){
        if (createdAt == null) createdAt = Instant.now();
    }
}
