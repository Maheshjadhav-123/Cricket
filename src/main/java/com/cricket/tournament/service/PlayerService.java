package com.cricket.tournament.service;

import com.cricket.tournament.entity.Player;
import com.cricket.tournament.entity.Team;
import com.cricket.tournament.repository.PlayerRepository;
import com.cricket.tournament.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/** CO3 — Service layer for Player business logic */
@Service
@Transactional
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamRepository   teamRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository, TeamRepository teamRepository) {
        this.playerRepository = playerRepository;
        this.teamRepository   = teamRepository;
    }

    @Transactional(readOnly = true)
    public List<Player> getAllPlayers() { return playerRepository.findAll(); }

    @Transactional(readOnly = true)
    public Optional<Player> getPlayerById(Long id) { return playerRepository.findById(id); }

    @Transactional(readOnly = true)
    public List<Player> getPlayersByTeam(Long teamId) { return playerRepository.findByTeamId(teamId); }

    public Player savePlayer(Player player) {
        if (player.getTeam() == null || player.getTeam().getId() == null)
            throw new IllegalArgumentException("Team ID is required.");
        Team team = teamRepository.findById(player.getTeam().getId())
                .orElseThrow(() -> new RuntimeException("Team not found."));
        player.setTeam(team);
        return playerRepository.save(player);
    }

    public Player updatePlayer(Long id, Player updated) {
        Player existing = playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found: " + id));
        existing.setPlayerName(updated.getPlayerName());
        existing.setRole(updated.getRole());
        existing.setJerseyNumber(updated.getJerseyNumber());
        existing.setAge(updated.getAge());
        existing.setNationality(updated.getNationality());
        if (updated.getTeam() != null && updated.getTeam().getId() != null) {
            Team t = teamRepository.findById(updated.getTeam().getId())
                    .orElseThrow(() -> new RuntimeException("Team not found."));
            existing.setTeam(t);
        }
        return playerRepository.save(existing);
    }

    public void deletePlayer(Long id) { playerRepository.deleteById(id); }
}