package com.arenacode.arenacode.problem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/**
 * Tag de classificação de problemas (ex.: "grafos", "dinamica"). {@code normalizedName} garante
 * unicidade case/acento-insensível básica (lowercase) e é derivado automaticamente de {@code
 * name}.
 */
@Entity
@Table(name = "tags")
public class Tag {

  public static final int MAX_NAME_LENGTH = 80;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid DEFAULT gen_random_uuid()")
  private UUID id;

  @Column(nullable = false, length = MAX_NAME_LENGTH)
  private String name;

  @Column(name = "normalized_name", nullable = false, unique = true, length = MAX_NAME_LENGTH)
  private String normalizedName;

  @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT NOW()")
  private Instant createdAt;

  protected Tag() {}

  public Tag(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    String trimmed = name.strip();
    if (trimmed.length() > MAX_NAME_LENGTH) {
      throw new IllegalArgumentException("name must have at most " + MAX_NAME_LENGTH + " chars");
    }
    this.name = trimmed;
    this.normalizedName = normalize(trimmed);
  }

  public static String normalize(String value) {
    return value.strip().toLowerCase(Locale.ROOT);
  }

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getNormalizedName() {
    return normalizedName;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
