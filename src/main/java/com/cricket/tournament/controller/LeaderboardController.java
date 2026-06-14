package com.cricket.tournament.controller;

import com.cricket.tournament.jdbc.JdbcReportDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Feature 8: Leaderboard & Ranking System Controller
 *
 * Provides sorted rankings for:
 *   - Top Batsmen (by total runs)
 *   - Top Bowlers (by wickets taken)
 *   - Best Teams (by win percentage)
 */
@RestController
@RequestMapping("/api/leaderboard")
@CrossOrigin(origins = "*")
public class LeaderboardController {

    private final JdbcReportDao jdbcReportDao;

    @Autowired
    public LeaderboardController(JdbcReportDao jdbcReportDao) {
        this.jdbcReportDao = jdbcReportDao;
    }

    /** GET /api/leaderboard/batsmen */
    @GetMapping("/batsmen")
    public ResponseEntity<List<Map<String, Object>>> topBatsmen() {
        return ResponseEntity.ok(jdbcReportDao.getTopBatsmen());
    }

    /** GET /api/leaderboard/bowlers */
    @GetMapping("/bowlers")
    public ResponseEntity<List<Map<String, Object>>> topBowlers() {
        return ResponseEntity.ok(jdbcReportDao.getTopBowlers());
    }

    /** GET /api/leaderboard/teams */
    @GetMapping("/teams")
    public ResponseEntity<List<Map<String, Object>>> teamRankings() {
        return ResponseEntity.ok(jdbcReportDao.getTeamRankings());
    }
}
