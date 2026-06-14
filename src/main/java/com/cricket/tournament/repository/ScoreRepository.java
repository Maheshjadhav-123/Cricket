package com.cricket.tournament.repository;

import com.cricket.tournament.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * CO3 — Spring Data JPA Repository for Score
 */
@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByMatchId(Long matchId);
    List<Score> findByTeamId(Long teamId);
}