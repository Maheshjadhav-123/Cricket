package com.cricket.tournament.repository;

import com.cricket.tournament.entity.AuctionBid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Feature 4 — JPA Repository for AuctionBid entity
 */
@Repository
public interface AuctionBidRepository extends JpaRepository<AuctionBid, Long> {
    List<AuctionBid> findByTeamId(Long teamId);
    List<AuctionBid> findByPlayerId(Long playerId);
    List<AuctionBid> findByStatus(String status);
    List<AuctionBid> findByPlayerIdAndStatus(Long playerId, String status);
}
