package com.cricket.tournament.service;

import com.cricket.tournament.entity.CricketMatch;
import com.cricket.tournament.entity.Score;
import com.cricket.tournament.entity.Team;
import com.cricket.tournament.repository.MatchRepository;
import com.cricket.tournament.repository.ScoreRepository;
import com.cricket.tournament.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/** CO3 — Service layer for Score business logic */
@Service
@Transactional
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final MatchRepository matchRepository;
    private final TeamRepository  teamRepository;

    @Autowired
    public ScoreService(ScoreRepository scoreRepository,
                        MatchRepository matchRepository,
                        TeamRepository  teamRepository) {
        this.scoreRepository = scoreRepository;
        this.matchRepository = matchRepository;
        this.teamRepository  = teamRepository;
    }

    @Transactional(readOnly = true)
    public List<Score> getAllScores() {
        return scoreRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Score> getScoreById(Long id) {
        return scoreRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Score> getScoresByMatch(Long matchId) {
        return scoreRepository.findByMatchId(matchId);
    }

    public Score saveScore(Score score) {
        CricketMatch match = matchRepository.findById(score.getMatch().getId())
                .orElseThrow(() -> new RuntimeException("Match not found."));
        Team team = teamRepository.findById(score.getTeam().getId())
                .orElseThrow(() -> new RuntimeException("Team not found."));

        // Validate team is part of this match
        if (!match.getTeam1().getId().equals(team.getId())
                && !match.getTeam2().getId().equals(team.getId())) {
            throw new IllegalArgumentException("Team is not part of this match.");
        }

        score.setMatch(match);
        score.setTeam(team);

        return scoreRepository.save(score);
    }

    public Score updateScore(Long id, Score updated) {
        Score existing = scoreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Score not found: " + id));

        existing.setRunsScored(updated.getRunsScored());
        existing.setWicketsLost(updated.getWicketsLost());
        existing.setOversPlayed(updated.getOversPlayed());
        existing.setExtras(updated.getExtras());
        existing.setInningNumber(updated.getInningNumber());

        return scoreRepository.save(existing);
    }

    public void deleteScore(Long id) {
        scoreRepository.deleteById(id);
    }

    // ✅ GET WINNER (🔥 NEW FEATURE)
    @Transactional(readOnly = true)
    public String getWinner(Long matchId) {
        List<Score> scores = scoreRepository.findByMatchId(matchId);

        if (scores.size() < 2) return "Match not completed";

        Score s1 = scores.get(0);
        Score s2 = scores.get(1);

        if (s1.getRunsScored() > s2.getRunsScored())
            return s1.getTeam().getTeamName();

        else if (s2.getRunsScored() > s1.getRunsScored())
            return s2.getTeam().getTeamName();

        else
            return "Tie";
    }
}