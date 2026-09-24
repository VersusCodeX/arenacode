package com.arenacode.arenacode.problem.domain;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Entity
@Table(name = "problems")
public class Problem {

  public static final int MAX_SLUG_LENGTH = 120;
  public static final int MAX_TITLE_LENGTH = 150;
  public static final int DEFAULT_TIME_LIMIT_MS = 2000;
  public static final int MIN_TIME_LIMIT_MS = 100;
  public static final int MAX_TIME_LIMIT_MS = 60_000;
  public static final int DEFAULT_MEMORY_LIMIT_KB = 262_144;
  public static final int MIN_MEMORY_LIMIT_KB = 16_384;
  public static final int MAX_MEMORY_LIMIT_KB = 2_097_152;

  private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid DEFAULT gen_random_uuid()")
  private UUID id;

  @Column(nullable = false, unique = true, length = MAX_SLUG_LENGTH)
  private String slug;

  @Column(nullable = false, length = MAX_TITLE_LENGTH)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String statement;

  @Column(name = "input_specification", columnDefinition = "TEXT")
  private String inputSpecification;

  @Column(name = "output_specification", columnDefinition = "TEXT")
  private String outputSpecification;

  @Column(name = "constraints_description", columnDefinition = "TEXT")
  private String constraintsDescription;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Difficulty difficulty;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'DRAFT'")
  private ProblemStatus status;

  @Column(name = "time_limit_ms", nullable = false, columnDefinition = "INTEGER DEFAULT 2000")
  private int timeLimitMs;

  @Column(name = "memory_limit_kb", nullable = false, columnDefinition = "INTEGER DEFAULT 262144")
  private int memoryLimitKb;

  /** Referencia por id ao usuario autor; sem associacao JPA para manter o isolamento de modulos. */
  @Column(name = "created_by")
  private UUID createdBy;

  @Version
  @Column(nullable = false)
  private long version;

  @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT NOW()")
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT NOW()")
  private Instant updatedAt;

  @Column(name = "published_at", columnDefinition = "TIMESTAMPTZ")
  private Instant publishedAt;

  @Column(name = "archived_at", columnDefinition = "TIMESTAMPTZ")
  private Instant archivedAt;

  @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("ordinal ASC")
  private List<TestCase> testCases = new ArrayList<>();

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "problem_tags",
      joinColumns = @JoinColumn(name = "problem_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private Set<Tag> tags = new HashSet<>();

  protected Problem() {}

  @SuppressFBWarnings(
      value = "CT_CONSTRUCTOR_THROW",
      justification =
          "Entidade JPA não pode ser final; validar no construtor é intencional e o risco"
              + " (finalizer attack) depende de finalize(), depreciado no Java (JEP 421).")
  public Problem(
      String slug, String title, String statement, Difficulty difficulty, UUID createdBy) {
    this.slug = validateSlug(slug);
    this.createdBy = createdBy;
    this.status = ProblemStatus.DRAFT;
    this.timeLimitMs = DEFAULT_TIME_LIMIT_MS;
    this.memoryLimitKb = DEFAULT_MEMORY_LIMIT_KB;
    applyDetails(title, statement, difficulty);
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

  public void updateDetails(String title, String statement, Difficulty difficulty) {
    ensureEditable();
    applyDetails(title, statement, difficulty);
  }

  public void updateSpecifications(
      String inputSpecification, String outputSpecification, String constraintsDescription) {
    ensureEditable();
    this.inputSpecification = blankToNull(inputSpecification);
    this.outputSpecification = blankToNull(outputSpecification);
    this.constraintsDescription = blankToNull(constraintsDescription);
  }

  public void changeLimits(int timeLimitMs, int memoryLimitKb) {
    ensureEditable();
    if (timeLimitMs < MIN_TIME_LIMIT_MS || timeLimitMs > MAX_TIME_LIMIT_MS) {
      throw new IllegalArgumentException(
          "timeLimitMs must be between " + MIN_TIME_LIMIT_MS + " and " + MAX_TIME_LIMIT_MS);
    }
    if (memoryLimitKb < MIN_MEMORY_LIMIT_KB || memoryLimitKb > MAX_MEMORY_LIMIT_KB) {
      throw new IllegalArgumentException(
          "memoryLimitKb must be between " + MIN_MEMORY_LIMIT_KB + " and " + MAX_MEMORY_LIMIT_KB);
    }
    this.timeLimitMs = timeLimitMs;
    this.memoryLimitKb = memoryLimitKb;
  }

  public void addTag(Tag tag) {
    ensureEditable();
    tags.add(Objects.requireNonNull(tag, "tag must not be null"));
  }

  public void removeTag(Tag tag) {
    ensureEditable();
    tags.remove(tag);
  }

  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP",
      justification = "Visão somente leitura; alterações passam por addTag/removeTag.")
  public Set<Tag> getTags() {
    return Collections.unmodifiableSet(tags);
  }

  public TestCase addTestCase(
      String input, String expectedOutput, TestCaseVisibility visibility, int weight) {
    ensureEditable();
    TestCase testCase =
        new TestCase(this, testCases.size() + 1, input, expectedOutput, visibility, weight);
    testCases.add(testCase);
    return testCase;
  }

  public void removeTestCase(TestCase testCase) {
    ensureEditable();
    if (!testCases.contains(testCase)) {
      throw new IllegalArgumentException("Test case does not belong to this problem");
    }
    ensureCanLoseEnabledPrivateCase(testCase);
    testCases.remove(testCase);
    renumberTestCases();
  }

  public List<TestCase> getPublicExamples() {
    return testCases.stream().filter(tc -> tc.isEnabled() && tc.isPublic()).toList();
  }

  public List<TestCase> getEnabledTestCases() {
    return testCases.stream().filter(TestCase::isEnabled).toList();
  }

  public boolean hasEnabledPrivateTestCase() {
    return testCases.stream().anyMatch(tc -> tc.isEnabled() && tc.isPrivate());
  }

  public boolean hasEnabledPublicTestCase() {
    return testCases.stream().anyMatch(tc -> tc.isEnabled() && tc.isPublic());
  }

  public boolean canBePublished() {
    return hasEnabledPublicTestCase() && hasEnabledPrivateTestCase();
  }

  public void publish() {
    if (status != ProblemStatus.DRAFT) {
      throw new IllegalStateException("Only DRAFT problems can be published");
    }
    if (!canBePublished()) {
      throw new IllegalStateException(
          "Problem must have at least one enabled PUBLIC and one enabled PRIVATE test case to be"
              + " published");
    }
    status = ProblemStatus.PUBLISHED;
    publishedAt = Instant.now();
  }

  public void archive() {
    if (status == ProblemStatus.ARCHIVED) {
      throw new IllegalStateException("Problem is already archived");
    }
    status = ProblemStatus.ARCHIVED;
    archivedAt = Instant.now();
  }

  public boolean canBeUsedInMatch() {
    return status == ProblemStatus.PUBLISHED && hasEnabledPrivateTestCase();
  }

  public boolean isPublished() {
    return status == ProblemStatus.PUBLISHED;
  }

  public boolean isArchived() {
    return status == ProblemStatus.ARCHIVED;
  }

  void ensureEditable() {
    if (status == ProblemStatus.ARCHIVED) {
      throw new IllegalStateException("Archived problems cannot be modified");
    }
  }

  void ensureCanLoseEnabledPrivateCase(TestCase testCase) {
    if (status != ProblemStatus.PUBLISHED || !testCase.isEnabled() || !testCase.isPrivate()) {
      return;
    }
    long enabledPrivate = testCases.stream().filter(tc -> tc.isEnabled() && tc.isPrivate()).count();
    if (enabledPrivate <= 1) {
      throw new IllegalStateException(
          "A published problem must keep at least one enabled PRIVATE test case");
    }
  }

  private void renumberTestCases() {
    for (int i = 0; i < testCases.size(); i++) {
      testCases.get(i).setOrdinal(i + 1);
    }
  }

  private void applyDetails(String title, String statement, Difficulty difficulty) {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("title must not be blank");
    }
    if (title.strip().length() > MAX_TITLE_LENGTH) {
      throw new IllegalArgumentException("title must have at most " + MAX_TITLE_LENGTH + " chars");
    }
    if (statement == null || statement.isBlank()) {
      throw new IllegalArgumentException("statement must not be blank");
    }
    if (difficulty == null) {
      throw new IllegalArgumentException("difficulty must not be null");
    }
    this.title = title.strip();
    this.statement = statement;
    this.difficulty = difficulty;
  }

  private static String validateSlug(String slug) {
    if (slug == null || slug.length() > MAX_SLUG_LENGTH || !SLUG_PATTERN.matcher(slug).matches()) {
      throw new IllegalArgumentException(
          "slug must be lowercase kebab-case (a-z, 0-9, '-') with at most "
              + MAX_SLUG_LENGTH
              + " chars");
    }
    return slug;
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  public UUID getId() { return id; }
  public String getSlug() { return slug; }
  public String getTitle() { return title; }
  public String getStatement() { return statement; }
  public String getInputSpecification() { return inputSpecification; }
  public String getOutputSpecification() { return outputSpecification; }
  public String getConstraintsDescription() { return constraintsDescription; }
  public Difficulty getDifficulty() { return difficulty; }
  public ProblemStatus getStatus() { return status; }
  public int getTimeLimitMs() { return timeLimitMs; }
  public int getMemoryLimitKb() { return memoryLimitKb; }
  public UUID getCreatedBy() { return createdBy; }
  public long getVersion() { return version; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public Instant getPublishedAt() { return publishedAt; }
  public Instant getArchivedAt() { return archivedAt; }
  public List<TestCase> getTestCases() { return Collections.unmodifiableList(testCases); }
}
