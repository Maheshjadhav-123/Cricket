package com.cricket.tournament.service;

import com.cricket.tournament.entity.CricketMatch;
import com.cricket.tournament.entity.Team;
import com.cricket.tournament.repository.MatchRepository;
import com.cricket.tournament.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/** CO3 — Service layer for CricketMatch business logic */
@Service
@Transactional
public class MatchService {

    private final MatchRepository matchRepository;
    private final TeamRepository  teamRepository;

    @Autowired
    public MatchService(MatchRepository matchRepository, TeamRepository teamRepository) {
        this.matchRepository = matchRepository;
        this.teamRepository  = teamRepository;
    }

    @Transactional(readOnly = true)
    public List<CricketMatch> getAllMatches() { return matchRepository.findAll(); }

    @Transactional(readOnly = true)
    public Optional<CricketMatch> getMatchById(Long id) { return matchRepository.findById(id); }

    @Transactional(readOnly = true)
    public List<CricketMatch> getLiveMatches() {
        return matchRepository.findByStatusIgnoreCase("Live");
    }

    public CricketMatch saveMatch(CricketMatch match) {
        if (match.getTeam1().getId().equals(match.getTeam2().getId()))
            throw new IllegalArgumentException("Team 1 and Team 2 cannot be the same.");
        Team t1 = teamRepository.findById(match.getTeam1().getId())
                .orElseThrow(() -> new RuntimeException("Team 1 not found."));
        Team t2 = teamRepository.findById(match.getTeam2().getId())
                .orElseThrow(() -> new RuntimeException("Team 2 not found."));
        match.setTeam1(t1);
        match.setTeam2(t2);
        return matchRepository.save(match);
    }

    public CricketMatch updateMatch(Long id, CricketMatch updated) {
        CricketMatch existing = matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found: " + id));
        existing.setVenue(updated.getVenue());
        existing.setMatchDate(updated.getMatchDate());
        existing.setMatchType(updated.getMatchType());
        existing.setStatus(updated.getStatus());
        if (updated.getTeam1() != null && updated.getTeam1().getId() != null) {
            existing.setTeam1(teamRepository.findById(updated.getTeam1().getId()).orElseThrow());
        }
        if (updated.getTeam2() != null && updated.getTeam2().getId() != null) {
            existing.setTeam2(teamRepository.findById(updated.getTeam2().getId()).orElseThrow());
        }
        return matchRepository.save(existing);
    }

    public void deleteMatch(Long id) { matchRepository.deleteById(id); }
}