package com.cricket.tournament.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.cricket.tournament.entity.Score;
import com.cricket.tournament.service.ScoreService;

@RestController
@RequestMapping("/api/scores")
@CrossOrigin(origins = "*")
public class ScoreController {

    @Autowired
    private ScoreService scoreService;

    // ✅ ADD SCORE (JSON BASED)
    @PostMapping
    public Score addScore(@RequestBody Score score) {
        return scoreService.saveScore(score);
    }

    // ✅ GET ALL SCORES
    @GetMapping
    public List<Score> getAllScores() {
        return scoreService.getAllScores();
    }

    // ✅ GET WINNER (🔥 NEW)
    @GetMapping("/winner/{matchId}")
    public String getWinner(@PathVariable Long matchId) {
        return scoreService.getWinner(matchId);
    }

    // ✅ DELETE SCORE
    @DeleteMapping("/{id}")
    public void deleteScore(@PathVariable Long id) {
        scoreService.deleteScore(id);
    }

    // ✅ UPDATE SCORE
    @PutMapping("/{id}")
    public Score updateScore(@PathVariable Long id, @RequestBody Score score) {
        score.setId(id);
        return scoreService.saveScore(score);
    }
}