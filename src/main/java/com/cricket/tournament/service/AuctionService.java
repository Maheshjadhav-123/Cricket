package com.cricket.tournament.service;

import com.cricket.tournament.entity.*;
import com.cricket.tournament.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Feature 4: IPL-Style Auction System Service
 *
 * Handles:
 *   - Setting team budgets
 *   - Placing bids for players
 *   - Auto-deducting purse on successful bids
 *   - Tracking sold/unsold players
 */
@Service
@Transactional
public class AuctionService {

    private final AuctionBidRepository auctionBidRepository;
    private final PlayerRepository     playerRepository;
    private final TeamRepository       teamRepository;
    private final NotificationService  notificationService;

    @Autowired
    public AuctionService(AuctionBidRepository auctionBidRepository,
                          PlayerRepository playerRepository,
                          TeamRepository teamRepository,
                          NotificationService notificationService) {
        this.auctionBidRepository = auctionBidRepository;
        this.playerRepository     = playerRepository;
        this.teamRepository       = teamRepository;
        this.notificationService  = notificationService;
    }

    /** Initialize budget for a team */
    public Team setBudget(Long teamId, Double budget) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));
        team.setBudget(budget);
        team.setPurseRemaining(budget);
        return teamRepository.save(team);
    }

    /** Set base price for a player */
    public Player setBasePrice(Long playerId, Double basePrice) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found: " + playerId));
        player.setBasePrice(basePrice);
        if (player.getAuctionStatus() == null) {
            player.setAuctionStatus("UNSOLD");
        }
        return playerRepository.save(player);
    }

    /**
     * Place a bid: team bids an amount for a player.
     * If bid >= base price and team has budget, player is SOLD to the team.
     */
    public Map<String, Object> placeBid(Long teamId, Long playerId, Double bidAmount) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found."));
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found."));

        Map<String, Object> result = new LinkedHashMap<>();

        // Check if player is already sold
        if ("SOLD".equals(player.getAuctionStatus())) {
            result.put("success", false);
            result.put("error", player.getPlayerName() + " is already sold!");
            return result;
        }

        // Check budget
        double purse = team.getPurseRemaining() != null ? team.getPurseRemaining() : 0;
        if (bidAmount > purse) {
            result.put("success", false);
            result.put("error", "Insufficient budget! Remaining: ₹" + purse + "L");
            return result;
        }

        // Check minimum bid (base price)
        double basePrice = player.getBasePrice() != null ? player.getBasePrice() : 0;
        if (bidAmount < basePrice) {
            result.put("success", false);
            result.put("error", "Bid must be >= base price (₹" + basePrice + "L)");
            return result;
        }

        // All good — SOLD!
        // Mark previous bids for this player as LOST
        List<AuctionBid> prevBids = auctionBidRepository.findByPlayerIdAndStatus(playerId, "PENDING");
        prevBids.forEach(b -> { b.setStatus("LOST"); auctionBidRepository.save(b); });

        // Save winning bid
        AuctionBid bid = new AuctionBid(team, player, bidAmount, "WON");
        auctionBidRepository.save(bid);

        // Update player
        player.setAuctionStatus("SOLD");
        player.setSoldPrice(bidAmount);
        player.setTeam(team);
        playerRepository.save(player);

        // Deduct from team purse
        team.setPurseRemaining(purse - bidAmount);
        teamRepository.save(team);

        // Create notification
        notificationService.createNotification(
            "💰 " + player.getPlayerName() + " SOLD to " + team.getTeamName() + " for ₹" + bidAmount + "L",
            "AUCTION", "💰"
        );

        result.put("success", true);
        result.put("message", player.getPlayerName() + " sold to " + team.getTeamName() + " for ₹" + bidAmount + "L");
        result.put("purseRemaining", team.getPurseRemaining());
        return result;
    }

    /** Get all sold players */
    @Transactional(readOnly = true)
    public List<Player> getSoldPlayers() {
        return playerRepository.findAll().stream()
                .filter(p -> "SOLD".equals(p.getAuctionStatus()))
                .toList();
    }

    /** Get all unsold players */
    @Transactional(readOnly = true)
    public List<Player> getUnsoldPlayers() {
        return playerRepository.findAll().stream()
                .filter(p -> !"SOLD".equals(p.getAuctionStatus()))
                .toList();
    }

    /** Get all bids */
    @Transactional(readOnly = true)
    public List<AuctionBid> getAllBids() {
        return auctionBidRepository.findAll();
    }

    /** Reset entire auction */
    public void resetAuction() {
        auctionBidRepository.deleteAll();
        playerRepository.findAll().forEach(p -> {
            p.setAuctionStatus("UNSOLD");
            p.setSoldPrice(null);
            playerRepository.save(p);
        });
        teamRepository.findAll().forEach(t -> {
            if (t.getBudget() != null) {
                t.setPurseRemaining(t.getBudget());
                teamRepository.save(t);
            }
        });
    }
}
