package com.cricket.tournament.repository;

import com.cricket.tournament.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * CO3 — Spring Data JPA Repository for Team
 * Auto-generates all CRUD SQL via Hibernate.
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByTeamNameIgnoreCase(String teamName);
    boolean existsByTeamNameIgnoreCase(String teamName);
}