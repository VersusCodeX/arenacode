package com.arenacode.arenacode.identity.domain;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private String email;
    private String passwordHash;
    private String displayName;
    private UserStatus status;
    private Integer currentRating;
    private boolean guest;

    protected User() {
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Integer getCurrentRating() {
        return currentRating;
    }

    public boolean isGuest() {
        return guest;
    }
}
