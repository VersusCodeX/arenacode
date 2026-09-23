package com.arenacode.arenacode.problem.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TestCaseDomainTest {

    private Problem newProblem() {
        return new Problem("sum", "Sum", "Some dois numeros", Difficulty.EASY, null);
    }

    @Test
    void shouldRejectNullContentOrVisibility() {
        Problem problem = newProblem();

        assertThatThrownBy(() -> problem.addTestCase(null, "1", TestCaseVisibility.PRIVATE, 1))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> problem.addTestCase("1", null, TestCaseVisibility.PRIVATE, 1))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> problem.addTestCase("1", "1", null, 1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldAllowEmptyInputAndOutput() {
        TestCase testCase = newProblem().addTestCase("", "", TestCaseVisibility.PRIVATE, 1);

        assertThat(testCase.getInput()).isEmpty();
        assertThat(testCase.getExpectedOutput()).isEmpty();
    }

    @Test
    void shouldRejectWeightOutOfRange() {
        Problem problem = newProblem();

        assertThatThrownBy(() -> problem.addTestCase("1", "1", TestCaseVisibility.PRIVATE, 0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> problem.addTestCase("1", "1", TestCaseVisibility.PRIVATE, 101))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectContentLargerThanLimit() {
        String tooLarge = "x".repeat(TestCase.MAX_CONTENT_BYTES + 1);

        assertThatThrownBy(
                () -> newProblem().addTestCase(tooLarge, "1", TestCaseVisibility.PRIVATE, 1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isPrivateAndIsPublicShouldReflectVisibility() {
        Problem problem = newProblem();
        TestCase privateCase = problem.addTestCase("1", "1", TestCaseVisibility.PRIVATE, 1);
        TestCase publicCase = problem.addTestCase("2", "2", TestCaseVisibility.PUBLIC, 1);

        assertThat(privateCase.isPrivate()).isTrue();
        assertThat(privateCase.isPublic()).isFalse();
        assertThat(publicCase.isPublic()).isTrue();
        assertThat(publicCase.isPrivate()).isFalse();
    }

    @Test
    void updateShouldReplaceContent() {
        TestCase testCase = newProblem().addTestCase("1", "1", TestCaseVisibility.PUBLIC, 1);

        testCase.update("5 5", "10", TestCaseVisibility.PRIVATE, 3);

        assertThat(testCase.getInput()).isEqualTo("5 5");
        assertThat(testCase.getExpectedOutput()).isEqualTo("10");
        assertThat(testCase.getVisibility()).isEqualTo(TestCaseVisibility.PRIVATE);
        assertThat(testCase.getWeight()).isEqualTo(3);
    }
}
