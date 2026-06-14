package com.cricket.tournament.controller;

import com.cricket.tournament.jdbc.JdbcReportDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Feature 3: Player Performance Analytics Controller
 *
 * Provides data for Chart.js visualization:
 *   - Per-player runs, averages, wickets
 *   - Team-wise run distribution
 */
@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final JdbcReportDao jdbcReportDao;

    @Autowired
    public AnalyticsController(JdbcReportDao jdbcReportDao) {
        this.jdbcReportDao = jdbcReportDao;
    }

    /** GET /api/analytics/player-stats — Full player analytics */
    @GetMapping("/player-stats")
    public ResponseEntity<List<Map<String, Object>>> getPlayerStats() {
        return ResponseEntity.ok(jdbcReportDao.getPlayerAnalytics());
    }

    /** GET /api/analytics/team-runs — Team-level run aggregation for charts */
    @GetMapping("/team-runs")
    public ResponseEntity<List<Map<String, Object>>> getTeamRuns() {
        return ResponseEntity.ok(jdbcReportDao.getTopBatsmen());
    }
}
