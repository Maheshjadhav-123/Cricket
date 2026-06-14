package com.cricket.tournament.repository;

import com.cricket.tournament.entity.CricketMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * CO3 — Spring Data JPA Repository for CricketMatch
 */
@Repository
public interface MatchRepository extends JpaRepository<CricketMatch, Long> {

    /** Find all matches where a team is either team1 or team2 */
    @Query("SELECT m FROM CricketMatch m WHERE m.team1.id = :teamId OR m.team2.id = :teamId")
    List<CricketMatch> findAllByTeamId(@Param("teamId") Long teamId);

    /** Find matches by status (e.g. "Live", "Scheduled") */
    List<CricketMatch> findByStatusIgnoreCase(String status);
}