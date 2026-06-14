package com.cricket.tournament.repository;

import com.cricket.tournament.entity.LiveEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * CO3 — Spring Data JPA Repository for LiveEvent
 * Used by the real-time socket feature (CO4).
 */
@Repository
public interface LiveEventRepository extends JpaRepository<LiveEvent, Long> {

    /** Fetch all events for a match, ordered newest first */
    List<LiveEvent> findByMatchIdOrderByCreatedAtDesc(Long matchId);

    /** Fetch latest N events for live score feed */
    List<LiveEvent> findTop10ByMatchIdOrderByCreatedAtDesc(Long matchId);
}