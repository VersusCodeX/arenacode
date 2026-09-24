package com.arenacode.arenacode.problem.adapter.out.persistence;

import com.arenacode.arenacode.problem.domain.Difficulty;
import com.arenacode.arenacode.problem.domain.Problem;
import com.arenacode.arenacode.problem.domain.Tag;
import com.arenacode.arenacode.problem.domain.TestCaseVisibility;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * Cobre constraints SQL de V4 com PostgreSQL real via Testcontainers: unicidade de tag
 * normalizada, ordem unica por problema, trigger de updated_at e relacionamento problem_tags.
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ProblemCatalogPersistenceIntegrationTest {

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Problem newProblem(String slug) {
        Problem problem = new Problem(slug, "Titulo", "Enunciado do problema.", Difficulty.EASY, null);
        problem.addTestCase("1", "1", TestCaseVisibility.PUBLIC, 1);
        problem.addTestCase("2", "2", TestCaseVisibility.PRIVATE, 1);
        return problem;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldRejectDuplicateNormalizedTagName() {
        tagRepository.saveAndFlush(new Tag("Grafos"));

        assertThatThrownBy(() -> new TransactionTemplate(transactionManager)
            .executeWithoutResult(status -> tagRepository.saveAndFlush(new Tag("grafos"))))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldPersistProblemTagsRelationship() {
        Tag graphs = tagRepository.save(new Tag("Grafos"));
        Tag dp = tagRepository.save(new Tag("Programacao Dinamica"));

        Problem problem = newProblem("problem-with-tags");
        problem.addTag(graphs);
        problem.addTag(dp);
        Problem saved = problemRepository.save(problem);
        flushAndClear();

        Problem loaded = problemRepository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getTags()).extracting(Tag::getNormalizedName)
            .containsExactlyInAnyOrder("grafos", "programacao dinamica");
    }

    @Test
    void deletingProblemShouldNotDeleteSharedTag() {
        Tag graphs = tagRepository.save(new Tag("Grafos"));
        Problem problem = newProblem("problem-tag-restrict");
        problem.addTag(graphs);
        Problem saved = problemRepository.save(problem);
        flushAndClear();

        problemRepository.deleteById(saved.getId());
        flushAndClear();

        assertThat(tagRepository.findById(graphs.getId())).isPresent();
    }

    @Test
    void shouldEnforceUniqueOrdinalPerProblemAtDatabaseLevel() {
        Problem problem = problemRepository.saveAndFlush(newProblem("ordinal-unique"));
        var problemId = problem.getId();

        assertThatThrownBy(() -> new TransactionTemplate(transactionManager)
            .executeWithoutResult(status -> entityManager
                .createNativeQuery(
                    "INSERT INTO test_cases (problem_id, ordinal, input, expected_output,"
                        + " visibility, weight, enabled) VALUES (:problemId, 1, 'x', 'x',"
                        + " 'PUBLIC', 1, true)")
                .setParameter("problemId", problemId)
                .executeUpdate()))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void updatedAtTriggerShouldBumpTimestampOnUpdate() throws InterruptedException {
        Problem problem = problemRepository.saveAndFlush(newProblem("trigger-updated-at"));
        Instant firstUpdatedAt = problem.getUpdatedAt();

        Thread.sleep(5);
        problem.updateDetails("Novo titulo", "Novo enunciado.", Difficulty.MEDIUM);
        problemRepository.saveAndFlush(problem);
        flushAndClear();

        Problem reloaded = problemRepository.findById(problem.getId()).orElseThrow();
        assertThat(reloaded.getUpdatedAt()).isAfter(firstUpdatedAt);
    }

    @Test
    void publishedProblemRequiresPublicAndPrivateEnabledTestCases() {
        Problem publicOnly = new Problem("public-only", "T", "E", Difficulty.EASY, null);
        publicOnly.addTestCase("1", "1", TestCaseVisibility.PUBLIC, 1);
        assertThatThrownBy(publicOnly::publish).isInstanceOf(IllegalStateException.class);

        Problem privateOnly = new Problem("private-only", "T", "E", Difficulty.EASY, null);
        privateOnly.addTestCase("1", "1", TestCaseVisibility.PRIVATE, 1);
        assertThatThrownBy(privateOnly::publish).isInstanceOf(IllegalStateException.class);

        Problem both = newProblem("public-and-private");
        both.publish();
        assertThat(both.isPublished()).isTrue();

        problemRepository.saveAndFlush(both);
        flushAndClear();
        assertThat(problemRepository.findBySlug("public-and-private").orElseThrow().isPublished())
            .isTrue();
    }
}
