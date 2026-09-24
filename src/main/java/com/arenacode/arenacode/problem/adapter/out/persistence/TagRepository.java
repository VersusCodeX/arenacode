package com.arenacode.arenacode.problem.adapter.out.persistence;

import com.arenacode.arenacode.problem.domain.Tag;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

  Optional<Tag> findByNormalizedName(String normalizedName);

  boolean existsByNormalizedName(String normalizedName);
}
