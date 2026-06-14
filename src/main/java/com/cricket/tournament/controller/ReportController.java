package com.cricket.tournament.controller;

import com.cricket.tournament.jdbc.JdbcReportDao;
import com.cricket.tournament.service.ReportGeneratorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CO1 (JDBC) + CO1 (Multithreading) — Report endpoints.
 * JDBC queries serve instant reports.
 * Async report generation runs in background thread pool.
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final JdbcReportDao           jdbcReportDao;
    private final ReportGeneratorService  reportGeneratorService;

    @Autowired
    public ReportController(JdbcReportDao jdbcReportDao,
                            ReportGeneratorService reportGeneratorService) {
        this.jdbcReportDao          = jdbcReportDao;
        this.reportGeneratorService = reportGeneratorService;
    }

    /** GET /api/reports/top-scorers — Raw JDBC query */
    @GetMapping("/top-scorers")
    public ResponseEntity<List<Map<String, Object>>> topScorers() {
        return ResponseEntity.ok(jdbcReportDao.getTopRunScorers());
    }

    /** GET /api/reports/team-stats — Win/Loss via raw JDBC */
    @GetMapping("/team-stats")
    public ResponseEntity<List<Map<String, Object>>> teamStats() {
        return ResponseEntity.ok(jdbcReportDao.getTeamWinLossStats());
    }

    /** GET /api/reports/matches-per-month */
    @GetMapping("/matches-per-month")
    public ResponseEntity<List<Map<String, Object>>> matchesPerMonth() {
        return ResponseEntity.ok(jdbcReportDao.getMatchesPerMonth());
    }

    /** GET /api/reports/dashboard-stats */
    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> dashboardStats() {
        return ResponseEntity.ok(jdbcReportDao.getDashboardStats());
    }

    /** GET /api/reports/players-by-city — SDG metric */
    @GetMapping("/players-by-city")
    public ResponseEntity<List<Map<String, Object>>> playersByCity() {
        return ResponseEntity.ok(jdbcReportDao.getPlayersByCity());
    }

    /** GET /api/reports/highest-scoring */
    @GetMapping("/highest-scoring")
    public ResponseEntity<List<Map<String, Object>>> highestScoring() {
        return ResponseEntity.ok(jdbcReportDao.getHighestScoringMatches());
    }

    /**
     * POST /api/reports/generate-async
     * CO1 Multithreading — triggers async report generation in thread pool.
     * Returns immediately with a task ID; report builds in background.
     */
    @PostMapping("/generate-async")
    public ResponseEntity<Map<String, Object>> generateAsync(HttpSession session) {
        Object role = session.getAttribute("userRole");
        if (!"ADMIN".equalsIgnoreCase(role != null ? role.toString() : "")) {
            return ResponseEntity.status(403)
                    .body(Map.of("error", "Admin access required."));
        }
        String taskId = reportGeneratorService.generateFullReportAsync();
        return ResponseEntity.ok(Map.of(
            "message", "Report generation started in background thread.",
            "taskId",  taskId,
            "note",    "CO1 Multithreading: ExecutorService thread pool is processing this."
        ));
    }
}