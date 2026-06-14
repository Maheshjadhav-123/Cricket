package com.cricket.tournament.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.cricket.tournament.entity.Player;
import com.cricket.tournament.entity.Team;
import com.cricket.tournament.service.PlayerService;
import com.cricket.tournament.service.TeamService;
@RestController
@RequestMapping("/api/players")
@CrossOrigin(origins = "*")
public class PlayerController {

    @Autowired
    private PlayerService service;

    @PostMapping
    public Player addPlayer(@RequestBody Player player) {
        return service.savePlayer(player);
    }

    @GetMapping
    public List<Player> getPlayers() {
        return service.getAllPlayers();
    }

    @GetMapping("/{id}")
    public Player getPlayer(@PathVariable Long id) {
        return service.getPlayerById(id)
                .orElseThrow(() -> new RuntimeException("Player not found"));
    }

    @PutMapping("/{id}")
    public Player updatePlayer(@PathVariable Long id, @RequestBody Player player) {
        return service.updatePlayer(id, player);
    }

    @DeleteMapping("/{id}")
    public void deletePlayer(@PathVariable Long id) {
        service.deletePlayer(id);
    }
}