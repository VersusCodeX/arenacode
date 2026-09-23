package com.arenacode.arenacode.problem.adapter.out.persistence;

import static org.assertj.core.api.Assertions.*;

import com.arenacode.arenacode.problem.domain.Difficulty;
import com.arenacode.arenacode.problem.domain.Problem;
import com.arenacode.arenacode.problem.domain.ProblemStatus;
import com.arenacode.arenacode.problem.domain.TestCase;
import com.arenacode.arenacode.problem.domain.TestCaseVisibility;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ProblemPersistenceIntegrationTest {

  @Autowired private ProblemRepository problemRepository;

  @Autowired private TestCaseRepository testCaseRepository;

  @Autowired private EntityManager entityManager;

  private Problem newProblem(String slug) {
    Problem problem = new Problem(slug, "Soma", "Some dois inteiros.", Difficulty.EASY, null);
    problem.updateSpecifications("Dois inteiros a e b", "A soma a + b", "|a|, |b| <= 10^9");
    problem.addTestCase("1 2\n", "3\n", TestCaseVisibility.PUBLIC, 1);
    problem.addTestCase("-5 5\n", "0\n", TestCaseVisibility.PRIVATE, 2);
    return problem;
  }

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void shouldPersistProblemWithTestCasesInCascade() {
    Problem saved = problemRepository.save(newProblem("persist-cascade"));
    flushAndClear();

    Problem loaded = problemRepository.findWithTestCasesById(saved.getId()).orElseThrow();

    assertThat(loaded.getSlug()).isEqualTo("persist-cascade");
    assertThat(loaded.getStatus()).isEqualTo(ProblemStatus.DRAFT);
    assertThat(loaded.getCreatedAt()).isNotNull();
    assertThat(loaded.getConstraintsDescription()).isEqualTo("|a|, |b| <= 10^9");
    assertThat(loaded.getTestCases())
        .extracting(TestCase::getOrdinal, TestCase::getVisibility, TestCase::getWeight)
        .containsExactly(
            tuple(1, TestCaseVisibility.PUBLIC, 1), tuple(2, TestCaseVisibility.PRIVATE, 2));
    assertThat(loaded.getTestCases().get(0).getInput()).isEqualTo("1 2\n");
  }

  @Test
  void shouldFindBySlug() {
    problemRepository.save(newProblem("find-by-slug"));
    flushAndClear();

    assertThat(problemRepository.findBySlug("find-by-slug")).isPresent();
    assertThat(problemRepository.existsBySlug("find-by-slug")).isTrue();
    assertThat(problemRepository.existsBySlug("does-not-exist")).isFalse();
  }

  @Test
  void shouldRejectDuplicateSlug() {
    problemRepository.saveAndFlush(newProblem("duplicate-slug"));

    assertThatThrownBy(() -> problemRepository.saveAndFlush(newProblem("duplicate-slug")))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void shouldRemoveOrphanTestCaseAndRenumber() {
    Problem saved = problemRepository.save(newProblem("orphan-removal"));
    saved.addTestCase("10 10\n", "20\n", TestCaseVisibility.PRIVATE, 1);
    flushAndClear();

    Problem loaded = problemRepository.findWithTestCasesById(saved.getId()).orElseThrow();
    loaded.removeTestCase(loaded.getTestCases().get(0));
    flushAndClear();

    List<TestCase> remaining = testCaseRepository.findByProblemIdOrderByOrdinalAsc(saved.getId());
    assertThat(remaining).extracting(TestCase::getOrdinal).containsExactly(1, 2);
    assertThat(remaining).extracting(TestCase::getInput).containsExactly("-5 5\n", "10 10\n");
  }

  @Test
  void shouldPersistPublicationAndQueryByStatus() {
    Problem problem = newProblem("published-problem");
    problem.publish();
    problemRepository.save(problem);
    problemRepository.save(newProblem("draft-problem"));
    flushAndClear();

    assertThat(problemRepository.findByStatus(ProblemStatus.PUBLISHED, PageRequest.of(0, 50)))
        .extracting(Problem::getSlug)
        .contains("published-problem")
        .doesNotContain("draft-problem");
    assertThat(
            problemRepository.findByStatusAndDifficulty(
                ProblemStatus.PUBLISHED, Difficulty.EASY, PageRequest.of(0, 50)))
        .extracting(Problem::getSlug)
        .contains("published-problem");
  }

  @Test
  void shouldQueryEnabledTestCasesByVisibility() {
    Problem saved = problemRepository.save(newProblem("visibility-query"));
    flushAndClear();

    assertThat(
            testCaseRepository.findByProblemIdAndVisibilityAndEnabledTrueOrderByOrdinalAsc(
                saved.getId(), TestCaseVisibility.PUBLIC))
        .extracting(TestCase::getExpectedOutput)
        .containsExactly("3\n");
    assertThat(testCaseRepository.findByProblemIdAndEnabledTrueOrderByOrdinalAsc(saved.getId()))
        .hasSize(2);
  }

  @Test
  void deletingProblemShouldCascadeToTestCases() {
    Problem saved = problemRepository.save(newProblem("delete-cascade"));
    flushAndClear();

    problemRepository.deleteById(saved.getId());
    flushAndClear();

    assertThat(testCaseRepository.findByProblemIdOrderByOrdinalAsc(saved.getId())).isEmpty();
  }
}
