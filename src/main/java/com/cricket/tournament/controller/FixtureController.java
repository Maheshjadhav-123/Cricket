package com.cricket.tournament.controller;

import com.cricket.tournament.service.FixtureService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Feature 6: Auto Fixture Generator Controller
 *
 * POST /api/fixtures/generate
 * Generates Round Robin or Knockout match schedules automatically.
 */
@RestController
@RequestMapping("/api/fixtures")
@CrossOrigin(origins = "*")
public class FixtureController {

    private final FixtureService fixtureService;

    @Autowired
    public FixtureController(FixtureService fixtureService) {
        this.fixtureService = fixtureService;
    }

    /**
     * POST /api/fixtures/generate
     * Body: { "format": "ROUND_ROBIN" | "KNOCKOUT", "matchType": "T20", "venue": "...", "startDate": "2026-05-01" }
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateFixtures(@RequestBody Map<String, String> body,
                                               HttpSession session) {
        Object role = session.getAttribute("userRole");
        if (!"ADMIN".equalsIgnoreCase(role != null ? role.toString() : "")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required."));
        }

        try {
            String format    = body.getOrDefault("format", "ROUND_ROBIN");
            String matchType = body.getOrDefault("matchType", "T20");
            String venue     = body.getOrDefault("venue", "Auto-Generated Venue");
            LocalDate startDate = body.containsKey("startDate") ?
                    LocalDate.parse(body.get("startDate")) : LocalDate.now();

            List<Map<String, Object>> fixtures;
            if ("KNOCKOUT".equalsIgnoreCase(format)) {
                fixtures = fixtureService.generateKnockout(matchType, venue, startDate);
            } else {
                fixtures = fixtureService.generateRoundRobin(matchType, venue, startDate);
            }

            return ResponseEntity.ok(Map.of(
                "message", format + " fixtures generated successfully!",
                "totalMatches", fixtures.size(),
                "fixtures", fixtures
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
