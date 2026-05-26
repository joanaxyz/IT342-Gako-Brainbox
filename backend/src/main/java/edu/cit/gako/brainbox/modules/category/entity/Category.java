package edu.cit.gako.brainbox.modules.category.entity;

import edu.cit.gako.brainbox.modules.user.entity.User;
import edu.cit.gako.brainbox.platform.security.interfaces.UserOwned;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Category implements UserOwned {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Instant createdAt;

    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
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
