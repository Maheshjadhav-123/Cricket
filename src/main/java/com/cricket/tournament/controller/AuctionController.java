package com.cricket.tournament.controller;

import com.cricket.tournament.entity.AuctionBid;
import com.cricket.tournament.entity.Player;
import com.cricket.tournament.entity.Team;
import com.cricket.tournament.service.AuctionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Feature 4: IPL-Style Auction System Controller
 *
 * Admin-only endpoints for managing the player auction:
 *   - Set team budgets
 *   - Set player base prices
 *   - Place bids
 *   - View sold/unsold players
 *   - Reset auction
 */
@RestController
@RequestMapping("/api/auction")
@CrossOrigin(origins = "*")
public class AuctionController {

    private final AuctionService auctionService;

    @Autowired
    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    private boolean isAdmin(HttpSession session) {
        Object role = session.getAttribute("userRole");
        return "ADMIN".equalsIgnoreCase(role != null ? role.toString() : "");
    }

    /** POST /api/auction/set-budget — Set team budget */
    @PostMapping("/set-budget")
    public ResponseEntity<?> setBudget(@RequestBody Map<String, Object> body, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Admin access required."));
        try {
            Long teamId = Long.parseLong(body.get("teamId").toString());
            Double budget = Double.parseDouble(body.get("budget").toString());
            Team team = auctionService.setBudget(teamId, budget);
            return ResponseEntity.ok(Map.of(
                "message", "Budget set for " + team.getTeamName(),
                "budget", team.getBudget(),
                "purseRemaining", team.getPurseRemaining()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** POST /api/auction/set-base-price — Set player base price */
    @PostMapping("/set-base-price")
    public ResponseEntity<?> setBasePrice(@RequestBody Map<String, Object> body, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Admin access required."));
        try {
            Long playerId = Long.parseLong(body.get("playerId").toString());
            Double basePrice = Double.parseDouble(body.get("basePrice").toString());
            Player player = auctionService.setBasePrice(playerId, basePrice);
            return ResponseEntity.ok(Map.of(
                "message", "Base price set for " + player.getPlayerName(),
                "basePrice", player.getBasePrice()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** POST /api/auction/bid — Place a bid */
    @PostMapping("/bid")
    public ResponseEntity<?> placeBid(@RequestBody Map<String, Object> body, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Admin access required."));
        try {
            Long teamId = Long.parseLong(body.get("teamId").toString());
            Long playerId = Long.parseLong(body.get("playerId").toString());
            Double bidAmount = Double.parseDouble(body.get("bidAmount").toString());
            Map<String, Object> result = auctionService.placeBid(teamId, playerId, bidAmount);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** GET /api/auction/sold — Get sold players */
    @GetMapping("/sold")
    public ResponseEntity<List<Player>> getSoldPlayers() {
        return ResponseEntity.ok(auctionService.getSoldPlayers());
    }

    /** GET /api/auction/unsold — Get unsold players */
    @GetMapping("/unsold")
    public ResponseEntity<List<Player>> getUnsoldPlayers() {
        return ResponseEntity.ok(auctionService.getUnsoldPlayers());
    }

    /** GET /api/auction/bids — Get all bids */
    @GetMapping("/bids")
    public ResponseEntity<List<AuctionBid>> getAllBids() {
        return ResponseEntity.ok(auctionService.getAllBids());
    }

    /** POST /api/auction/reset — Reset entire auction */
    @PostMapping("/reset")
    public ResponseEntity<?> resetAuction(HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Admin access required."));
        auctionService.resetAuction();
        return ResponseEntity.ok(Map.of("message", "Auction reset successfully."));
    }
}
