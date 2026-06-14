package com.cricket.tournament.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.cricket.tournament.entity.Team;
import com.cricket.tournament.service.TeamService;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "*")
public class TeamController {

    @Autowired
    private TeamService service;

    // ✅ ADD TEAM
    @PostMapping
    public Team addTeam(@RequestBody Team team) {
        return service.saveTeam(team);
    }

    // ✅ GET ALL TEAMS
    @GetMapping
    public List<Team> getTeams() {
        return service.getAllTeams();
    }

    // ✅ GET TEAM BY ID (🔥 THIS WAS MISSING)
    @GetMapping("/{id}")
    public Team getTeamById(@PathVariable Long id) {
        return service.getTeamById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));
    }

    // ✅ UPDATE TEAM
    @PutMapping("/{id}")
    public Team updateTeam(@PathVariable Long id, @RequestBody Team team) {
        return service.updateTeam(id, team);
    }

    // ✅ DELETE TEAM
    @DeleteMapping("/{id}")
    public void deleteTeam(@PathVariable Long id) {
        service.deleteTeam(id);
    }
}