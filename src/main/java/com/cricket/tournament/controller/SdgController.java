package com.cricket.tournament.controller;

import com.cricket.tournament.jdbc.JdbcReportDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * STEP 8 — SDG Dashboard Controller.
 *
 * SDG 3  (Good Health): tracks active players = physical activity proxy
 * SDG 11 (Communities): tracks matches per city = community engagement
 *
 * All metrics come from raw JDBC queries (CO1).
 */
@RestController
@RequestMapping("/api/sdg")
@CrossOrigin(origins = "*")
public class SdgController {

    private final JdbcReportDao jdbcReportDao;

    @GetMapping("/sdg-metrics")
    public ResponseEntity<Map<String, Object>> getSdgMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("sdg", "SDG 3: Good Health and Well-being");
        metrics.put("metric", "Active Players Reached");
        metrics.put("value", jdbcReportDao.getDashboardStats().get("total_players"));
        return ResponseEntity.ok(metrics);
    }

    @Autowired
    public SdgController(JdbcReportDao jdbcReportDao) {
        this.jdbcReportDao = jdbcReportDao;
    }

    /**
     * GET /api/sdg/impact
     * Returns all SDG impact metrics in one response.
     */
    @GetMapping("/impact")
    public ResponseEntity<Map<String, Object>> getImpactMetrics() {
        Map<String, Object> stats = jdbcReportDao.getDashboardStats();

        Map<String, Object> impact = new LinkedHashMap<>();

        // SDG 3 — Physical Activity
        impact.put("sdg3_title",         "SDG 3: Good Health & Well-Being");
        impact.put("sdg3_metric",        "Active Players Registered");
        impact.put("sdg3_value",         stats.get("total_players"));
        impact.put("sdg3_description",   "Each registered player represents an individual engaged in regular physical sports activity.");

        // SDG 11 — Community
        impact.put("sdg11_title",        "SDG 11: Sustainable Communities");
        impact.put("sdg11_metric",       "Community Matches Organised");
        impact.put("sdg11_value",        stats.get("total_matches"));
        impact.put("sdg11_description",  "Each match is a community gathering event promoting social cohesion.");

        // SDG 4 — Education (coaches + analytics)
        impact.put("sdg4_title",         "SDG 4: Quality Education");
        impact.put("sdg4_metric",        "Teams with Coaching Staff");
        impact.put("sdg4_value",         stats.get("total_teams"));
        impact.put("sdg4_description",   "Coaches use analytics data from this platform to improve player training.");

        // Regional reach (from JDBC)
        impact.put("regional_breakdown", jdbcReportDao.getPlayersByCity());
        impact.put("monthly_activity",   jdbcReportDao.getMatchesPerMonth());

        return ResponseEntity.ok(impact);
    }
}