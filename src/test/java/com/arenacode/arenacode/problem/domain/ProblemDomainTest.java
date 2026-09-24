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
        assertThat(problem.getTags()).isEmpty();
        assertThat(problem.canBeUsedInMatch()).isFalse();
        assertThat(problem.canBePublished()).isFalse();
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
        assertThatThrownBy(() -> problem.changeLimits(Problem.MAX_TIME_LIMIT_MS + 1, Problem.DEFAULT_MEMORY_LIMIT_KB))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> problem.changeLimits(1000, Problem.MAX_MEMORY_LIMIT_KB + 1))
            .isInstanceOf(IllegalArgumentException.class);

        problem.changeLimits(1000, 131_072);
        assertThat(problem.getTimeLimitMs()).isEqualTo(1000);
        assertThat(problem.getMemoryLimitKb()).isEqualTo(131_072);

        problem.changeLimits(Problem.MAX_TIME_LIMIT_MS, Problem.MAX_MEMORY_LIMIT_KB);
        assertThat(problem.getTimeLimitMs()).isEqualTo(60_000);
        assertThat(problem.getMemoryLimitKb()).isEqualTo(2_097_152);
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
    void tagsSetShouldBeUnmodifiable() {
        Problem problem = newProblem();

        assertThatThrownBy(() -> problem.getTags().add(new Tag("grafos")))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void addTagAndRemoveTagShouldUpdateProblemTags() {
        Problem problem = newProblem();
        Tag graphs = new Tag("Grafos");

        problem.addTag(graphs);
        assertThat(problem.getTags()).containsExactly(graphs);

        problem.removeTag(graphs);
        assertThat(problem.getTags()).isEmpty();
    }

    @Test
    void addTagShouldRejectNull() {
        Problem problem = newProblem();

        assertThatThrownBy(() -> problem.addTag(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void hasEnabledPublicTestCaseShouldReflectEnabledPublicCases() {
        Problem problem = newProblem();
        assertThat(problem.hasEnabledPublicTestCase()).isFalse();

        TestCase publicCase = problem.addTestCase("1", "1", TestCaseVisibility.PUBLIC, 1);
        assertThat(problem.hasEnabledPublicTestCase()).isTrue();

        publicCase.disable();
        assertThat(problem.hasEnabledPublicTestCase()).isFalse();
    }

    @Test
    void publishShouldRequireEnabledPublicAndPrivateTestCase() {
        Problem problem = newProblem();
        problem.addTestCase("1", "1", TestCaseVisibility.PUBLIC, 1);

        assertThat(problem.canBePublished()).isFalse();
        assertThatThrownBy(problem::publish).isInstanceOf(IllegalStateException.class);

        TestCase privateCase = problem.addTestCase("2", "2", TestCaseVisibility.PRIVATE, 1);
        privateCase.disable();
        assertThat(problem.canBePublished()).isFalse();
        assertThatThrownBy(problem::publish).isInstanceOf(IllegalStateException.class);

        privateCase.enable();
        assertThat(problem.canBePublished()).isTrue();
        problem.publish();

        assertThat(problem.isPublished()).isTrue();
        assertThat(problem.getPublishedAt()).isNotNull();
        assertThat(problem.canBeUsedInMatch()).isTrue();
    }

    @Test
    void publishShouldRejectWhenOnlyPrivateTestCaseExists() {
        Problem problem = newProblem();
        problem.addTestCase("1", "1", TestCaseVisibility.PRIVATE, 1);

        assertThat(problem.canBePublished()).isFalse();
        assertThatThrownBy(problem::publish).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void publishShouldOnlyWorkFromDraft() {
        Problem problem = newProblem();
        problem.addTestCase("1", "1", TestCaseVisibility.PUBLIC, 1);
        problem.addTestCase("2", "2", TestCaseVisibility.PRIVATE, 1);
        problem.publish();

        assertThatThrownBy(problem::publish).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void publishedProblemShouldKeepAtLeastOneEnabledPrivateCase() {
        Problem problem = newProblem();
        TestCase publicCase = problem.addTestCase("0", "0", TestCaseVisibility.PUBLIC, 1);
        TestCase privateCase = problem.addTestCase("1", "1", TestCaseVisibility.PRIVATE, 1);
        problem.publish();

        assertThatThrownBy(privateCase::disable).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> problem.removeTestCase(privateCase))
            .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> privateCase.update("1", "1", TestCaseVisibility.PUBLIC, 1))
            .isInstanceOf(IllegalStateException.class);

        TestCase anotherPrivate = problem.addTestCase("2", "2", TestCaseVisibility.PRIVATE, 1);
        privateCase.disable();

        assertThat(privateCase.isEnabled()).isFalse();
        assertThat(problem.getEnabledTestCases()).containsExactly(publicCase, anotherPrivate);
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
        assertThatThrownBy(() -> problem.addTag(new Tag("grafos")))
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
