package com.example.brainbox_api.auth.entity;

import java.time.Instant;

import com.example.brainbox_api.auth.enumeration.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
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
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String username;
    @Column(unique = true)
    private String email;
    private String password;

    @Builder.Default
    private boolean banned = false;
    @Builder.Default
    private boolean verified = false;
    @Enumerated(EnumType.STRING)
    private UserRole role;

    private Instant lastLogin;
    private Instant lastLogout;
    @Column (nullable = false)
    private Instant createdAt;

    public void updateLastLogin() {
        lastLogin = Instant.now();
    }

    public void updateLastLogout() {
        lastLogout = Instant.now();
    }

    @PrePersist
    private void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
