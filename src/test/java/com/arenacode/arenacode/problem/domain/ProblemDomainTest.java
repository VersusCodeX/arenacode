package com.arenacode.arenacode.problem.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ProblemDomainTest {

    private Problem newProblem() {
        return new Problem("two-sum", "Two Sum", "Dado um array...", Difficulty.EASY, UUID.randomUUID());
    }

    @Test
    void newProblemShouldStartAsDraftWithDefaultLimits() {
        Problem problem = newProblem();

        assertThat(problem.getStatus()).isEqualTo(ProblemStatus.DRAFT);
        assertThat(problem.getTimeLimitMs()).isEqualTo(Problem.DEFAULT_TIME_LIMIT_MS);
        assertThat(problem.getMemoryLimitKb()).isEqualTo(Problem.DEFAULT_MEMORY_LIMIT_KB);
        assertThat(problem.getTestCases()).isEmpty();
        assertThat(problem.canBeUsedInMatch()).isFalse();
    }

    @Test
    void constructorShouldRejectInvalidSlug() {
        assertThatThrownBy(() -> new Problem("Two Sum", "Two Sum", "x", Difficulty.EASY, null))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Problem("two--sum", "Two Sum", "x", Difficulty.EASY, null))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Problem(null, "Two Sum", "x", Difficulty.EASY, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructorShouldRejectBlankTitleStatementOrNullDifficulty() {
        assertThatThrownBy(() -> new Problem("a", " ", "x", Difficulty.EASY, null))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Problem("a", "A", "  ", Difficulty.EASY, null))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Problem("a", "A", "x", null, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void changeLimitsShouldRejectOutOfRangeValues() {
        Problem problem = newProblem();

        assertThatThrownBy(() -> problem.changeLimits(50, Problem.DEFAULT_MEMORY_LIMIT_KB))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> problem.changeLimits(1000, 1024))
            .isInstanceOf(IllegalArgumentException.class);

        problem.changeLimits(1000, 131_072);
        assertThat(problem.getTimeLimitMs()).isEqualTo(1000);
        assertThat(problem.getMemoryLimitKb()).isEqualTo(131_072);
    }

    @Test
    void addTestCaseShouldAssignSequentialOrdinals() {
        Problem problem = newProblem();

        TestCase first = problem.addTestCase("1 2", "3", TestCaseVisibility.PUBLIC, 1);
        TestCase second = problem.addTestCase("2 2", "4", TestCaseVisibility.PRIVATE, 5);

        assertThat(first.getOrdinal()).isEqualTo(1);
        assertThat(second.getOrdinal()).isEqualTo(2);
        assertThat(second.getProblem()).isSameAs(problem);
        assertThat(second.isEnabled()).isTrue();
    }

    @Test
    void removeTestCaseShouldRenumberRemainingCases() {
        Problem problem = newProblem();
        TestCase first = problem.addTestCase("a", "a", TestCaseVisibility.PUBLIC, 1);
        TestCase second = problem.addTestCase("b", "b", TestCaseVisibility.PRIVATE, 1);
        TestCase third = problem.addTestCase("c", "c", TestCaseVisibility.PRIVATE, 1);

        problem.removeTestCase(first);

        assertThat(problem.getTestCases()).containsExactly(second, third);
        assertThat(second.getOrdinal()).isEqualTo(1);
        assertThat(third.getOrdinal()).isEqualTo(2);
    }

    @Test
    void testCasesListShouldBeUnmodifiable() {
        Problem problem = newProblem();

        assertThatThrownBy(() -> problem.getTestCases().clear())
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void publishShouldRequireEnabledPrivateTestCase() {
        Problem problem = newProblem();
        problem.addTestCase("1", "1", TestCaseVisibility.PUBLIC, 1);

        assertThatThrownBy(problem::publish).isInstanceOf(IllegalStateException.class);

        TestCase privateCase = problem.addTestCase("2", "2", TestCaseVisibility.PRIVATE, 1);
        privateCase.disable();
        assertThatThrownBy(problem::publish).isInstanceOf(IllegalStateException.class);

        privateCase.enable();
        problem.publish();

        assertThat(problem.isPublished()).isTrue();
        assertThat(problem.getPublishedAt()).isNotNull();
        assertThat(problem.canBeUsedInMatch()).isTrue();
    }

    @Test
    void publishShouldOnlyWorkFromDraft() {
        Problem problem = newProblem();
        problem.addTestCase("1", "1", TestCaseVisibility.PRIVATE, 1);
        problem.publish();

        assertThatThrownBy(problem::publish).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void publishedProblemShouldKeepAtLeastOneEnabledPrivateCase() {
        Problem problem = newProblem();
        TestCase privateCase = problem.addTestCase("1", "1", TestCaseVisibility.PRIVATE, 1);
        problem.publish();

        assertThatThrownBy(privateCase::disable).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> problem.removeTestCase(privateCase))
            .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> privateCase.update("1", "1", TestCaseVisibility.PUBLIC, 1))
            .isInstanceOf(IllegalStateException.class);

        TestCase another = problem.addTestCase("2", "2", TestCaseVisibility.PRIVATE, 1);
        privateCase.disable();

        assertThat(privateCase.isEnabled()).isFalse();
        assertThat(problem.getEnabledTestCases()).containsExactly(another);
        assertThat(problem.canBeUsedInMatch()).isTrue();
    }

    @Test
    void archivedProblemShouldRejectModifications() {
        Problem problem = newProblem();
        TestCase testCase = problem.addTestCase("1", "1", TestCaseVisibility.PRIVATE, 1);
        problem.archive();

        assertThat(problem.isArchived()).isTrue();
        assertThat(problem.getArchivedAt()).isNotNull();
        assertThat(problem.canBeUsedInMatch()).isFalse();
        assertThatThrownBy(() -> problem.updateDetails("New", "New", Difficulty.HARD))
            .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> problem.addTestCase("2", "2", TestCaseVisibility.PRIVATE, 1))
            .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(testCase::disable).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(problem::archive).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(problem::publish).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void publicExamplesShouldOnlyContainEnabledPublicCases() {
        Problem problem = newProblem();
        TestCase example = problem.addTestCase("1", "1", TestCaseVisibility.PUBLIC, 1);
        TestCase disabledExample = problem.addTestCase("2", "2", TestCaseVisibility.PUBLIC, 1);
        problem.addTestCase("3", "3", TestCaseVisibility.PRIVATE, 1);
        disabledExample.disable();

        assertThat(problem.getPublicExamples()).containsExactly(example);
        assertThat(problem.hasEnabledPrivateTestCase()).isTrue();
    }
}
