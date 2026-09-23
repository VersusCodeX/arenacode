package com.arenacode.arenacode.problem.domain;

import jakarta.persistence.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "test_cases")
public class TestCase {

  public static final int MIN_WEIGHT = 1;
  public static final int MAX_WEIGHT = 100;
  public static final int MAX_CONTENT_BYTES = 1_048_576;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid DEFAULT gen_random_uuid()")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "problem_id", nullable = false)
  private Problem problem;

  @Column(nullable = false)
  private int ordinal;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String input;

  @Column(name = "expected_output", nullable = false, columnDefinition = "TEXT")
  private String expectedOutput;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TestCaseVisibility visibility;

  @Column(nullable = false)
  private int weight;

  @Column(nullable = false)
  private boolean enabled;

  @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT NOW()")
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT NOW()")
  private Instant updatedAt;

  protected TestCase() {}

  /** Criado apenas via {@link Problem#addTestCase}, que controla o ordinal. */
  TestCase(
      Problem problem,
      int ordinal,
      String input,
      String expectedOutput,
      TestCaseVisibility visibility,
      int weight) {
    this.problem = Objects.requireNonNull(problem, "problem must not be null");
    this.ordinal = ordinal;
    this.enabled = true;
    applyContent(input, expectedOutput, visibility, weight);
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

  public void update(
      String input, String expectedOutput, TestCaseVisibility visibility, int weight) {
    problem.ensureEditable();
    if (visibility == TestCaseVisibility.PUBLIC) {
      problem.ensureCanLoseEnabledPrivateCase(this);
    }
    applyContent(input, expectedOutput, visibility, weight);
  }

  public void enable() {
    problem.ensureEditable();
    enabled = true;
  }

  public void disable() {
    problem.ensureEditable();
    problem.ensureCanLoseEnabledPrivateCase(this);
    enabled = false;
  }

  public boolean isPrivate() { return visibility == TestCaseVisibility.PRIVATE; }
  public boolean isPublic() { return visibility == TestCaseVisibility.PUBLIC; }

  void setOrdinal(int ordinal) { this.ordinal = ordinal; }

  private void applyContent(
      String input, String expectedOutput, TestCaseVisibility visibility, int weight) {
    validateContent(input, "input");
    validateContent(expectedOutput, "expectedOutput");
    if (visibility == null) {
      throw new IllegalArgumentException("visibility must not be null");
    }
    if (weight < MIN_WEIGHT || weight > MAX_WEIGHT) {
      throw new IllegalArgumentException(
          "weight must be between " + MIN_WEIGHT + " and " + MAX_WEIGHT);
    }
    this.input = input;
    this.expectedOutput = expectedOutput;
    this.visibility = visibility;
    this.weight = weight;
  }

  private static void validateContent(String value, String field) {
    if (value == null) {
      throw new IllegalArgumentException(field + " must not be null");
    }
    if (value.getBytes(StandardCharsets.UTF_8).length > MAX_CONTENT_BYTES) {
      throw new IllegalArgumentException(field + " exceeds " + MAX_CONTENT_BYTES + " bytes");
    }
  }

  public UUID getId() { return id; }
  public Problem getProblem() { return problem; }
  public int getOrdinal() { return ordinal; }
  public String getInput() { return input; }
  public String getExpectedOutput() { return expectedOutput; }
  public TestCaseVisibility getVisibility() { return visibility; }
  public int getWeight() { return weight; }
  public boolean isEnabled() { return enabled; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
