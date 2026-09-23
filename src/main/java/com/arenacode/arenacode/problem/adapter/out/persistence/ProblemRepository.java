package com.arenacode.arenacode.problem.adapter.out.persistence;

import com.arenacode.arenacode.problem.domain.Difficulty;
import com.arenacode.arenacode.problem.domain.Problem;
import com.arenacode.arenacode.problem.domain.ProblemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, UUID> {

    Optional<Problem> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = "testCases")
    Optional<Problem> findWithTestCasesById(UUID id);

    Page<Problem> findByStatus(ProblemStatus status, Pageable pageable);

    Page<Problem> findByStatusAndDifficulty(
            ProblemStatus status, Difficulty difficulty, Pageable pageable);
}
