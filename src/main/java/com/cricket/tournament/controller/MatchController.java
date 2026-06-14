package com.cricket.tournament.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.cricket.tournament.entity.CricketMatch;
import com.cricket.tournament.service.MatchService;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "*")
public class MatchController {

    @Autowired
    private MatchService service;

    // ✅ ADD MATCH (JSON based)
    @PostMapping
    public CricketMatch addMatch(@RequestBody CricketMatch match) {
        return service.saveMatch(match);
    }

    // ✅ GET ALL MATCHES
    @GetMapping
    public List<CricketMatch> getMatches() {
        return service.getAllMatches();
    }

    // ✅ GET MATCH BY ID
    @GetMapping("/{id}")
    public CricketMatch getMatchById(@PathVariable Long id) {
        return service.getMatchById(id)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
    }

    // ✅ DELETE MATCH
    @DeleteMapping("/{id}")
    public String deleteMatch(@PathVariable Long id) {
        service.deleteMatch(id);
        return "Match deleted successfully";
    }
}