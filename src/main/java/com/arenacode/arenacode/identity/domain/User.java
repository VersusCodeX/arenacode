package com.arenacode.arenacode.identity.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid DEFAULT gen_random_uuid()")
  private UUID id;

  @Column(columnDefinition = "CITEXT")
  private String email;

  @Column(name = "password_hash", length = 255)
  private String passwordHash;

  @Column(name = "display_name", nullable = false, length = 80)
  private String displayName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserStatus status;

  @Column(name = "is_guest", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
  private boolean guest;

  @Column(name = "current_rating", nullable = false, columnDefinition = "INTEGER DEFAULT 1000")
  private int currentRating;

  @Enumerated(EnumType.STRING)
  @Column(
      name = "preferred_language",
      nullable = false,
      length = 30,
      columnDefinition = "VARCHAR(30) DEFAULT 'JAVA_21'")
  private ProgrammingLanguage preferredLanguage;

  @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT NOW()")
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT NOW()")
  private Instant updatedAt;

  @Column(name = "deleted_at", columnDefinition = "TIMESTAMPTZ")
  private Instant deletedAt;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  protected User() {}

  public User(String displayName, UserStatus status, boolean guest) {
    this.displayName = displayName;
    this.status = status;
    this.guest = guest;
    this.currentRating = 1000;
    this.preferredLanguage = ProgrammingLanguage.JAVA_21;
  }

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    if (createdAt == null) createdAt = now;
    if (updatedAt == null) updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public boolean isGuest() {
    return guest;
  }

  public void setGuest(boolean guest) {
    this.guest = guest;
  }

  public int getCurrentRating() {
    return currentRating;
  }

  public void setCurrentRating(int currentRating) {
    this.currentRating = currentRating;
  }

  public ProgrammingLanguage getPreferredLanguage() {
    return preferredLanguage;
  }

  public void setPreferredLanguage(ProgrammingLanguage preferredLanguage) {
    this.preferredLanguage = preferredLanguage;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Instant getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(Instant deletedAt) {
    this.deletedAt = deletedAt;
  }

  /** Visão somente leitura; para adicionar papéis use {@link #addRole(Role)}. */
  public Set<Role> getRoles() {
    return Collections.unmodifiableSet(roles);
  }

  public void setRoles(Set<Role> roles) {
    this.roles = new HashSet<>(roles);
  }

  public void addRole(Role role) {
    roles.add(role);
  }

  public boolean isActive() {
    return status == UserStatus.ACTIVE;
  }

  public boolean canAuthenticate() {
    return isActive() && !guest && email != null;
  }

  public boolean hasRole(String roleCode) {
    return roles.stream().anyMatch(role -> role.getCode().equalsIgnoreCase(roleCode));
  }
}
