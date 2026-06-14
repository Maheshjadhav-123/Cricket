package com.cricket.tournament.controller;

import com.cricket.tournament.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Feature 1: AI-Based Match Prediction Controller
 *
 * GET /api/predict/{team1Id}/{team2Id}
 * Returns predicted winner, win probabilities, and analysis breakdown.
 */
@RestController
@RequestMapping("/api/predict")
@CrossOrigin(origins = "*")
public class PredictionController {

    private final PredictionService predictionService;

    @Autowired
    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @GetMapping("/{team1Id}/{team2Id}")
    public ResponseEntity<Map<String, Object>> predict(
            @PathVariable Long team1Id,
            @PathVariable Long team2Id) {
        try {
            Map<String, Object> prediction = predictionService.predictMatch(team1Id, team2Id);
            return ResponseEntity.ok(prediction);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
