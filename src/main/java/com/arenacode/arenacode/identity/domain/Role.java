package com.arenacode.arenacode.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid DEFAULT gen_random_uuid()")
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    protected Role() {
    }

    public Role(String code) {
        this.code = code;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }
}
