package com.arenacode.arenacode.problem.adapter.out.persistence;

import com.arenacode.arenacode.problem.domain.TestCase;
import com.arenacode.arenacode.problem.domain.TestCaseVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Leitura de casos de teste. Criacao, alteracao e remocao devem passar pelo agregado {@code
 * Problem}, que mantem ordinais e invariantes de publicacao.
 */
@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, UUID> {

    List<TestCase> findByProblemIdOrderByOrdinalAsc(UUID problemId);

    List<TestCase> findByProblemIdAndEnabledTrueOrderByOrdinalAsc(UUID problemId);

    List<TestCase> findByProblemIdAndVisibilityAndEnabledTrueOrderByOrdinalAsc(
            UUID problemId, TestCaseVisibility visibility);
}
