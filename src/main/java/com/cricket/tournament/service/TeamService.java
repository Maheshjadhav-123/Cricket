package com.cricket.tournament.service;

import com.cricket.tournament.entity.Team;
import com.cricket.tournament.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/** CO3 — Service layer for Team business logic */
@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;

    @Autowired
    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public List<Team> getAllTeams() { return teamRepository.findAll(); }

    @Transactional(readOnly = true)
    public Optional<Team> getTeamById(Long id) { return teamRepository.findById(id); }

    public Team saveTeam(Team team) {
        if (team.getId() == null && teamRepository.existsByTeamNameIgnoreCase(team.getTeamName()))
            throw new IllegalArgumentException("Team '" + team.getTeamName() + "' already exists.");
        return teamRepository.save(team);
    }

    public Team updateTeam(Long id, Team updated) {
        Team existing = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found: " + id));
        existing.setTeamName(updated.getTeamName());
        existing.setCoachName(updated.getCoachName());
        existing.setHomeCity(updated.getHomeCity());
        existing.setFoundedYear(updated.getFoundedYear());
        return teamRepository.save(existing);
    }

    public void deleteTeam(Long id) {
        if (!teamRepository.existsById(id))
            throw new RuntimeException("Team not found: " + id);
        teamRepository.deleteById(id);
    }
}