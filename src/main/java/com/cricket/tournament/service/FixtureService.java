package com.cricket.tournament.service;

import com.cricket.tournament.entity.CricketMatch;
import com.cricket.tournament.entity.Team;
import com.cricket.tournament.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Feature 6: Auto Fixture Generator
 *
 * Generates match schedules automatically using:
 *   - Round Robin: every team plays every other team once
 *   - Knockout: single-elimination bracket
 */
@Service
@Transactional
public class FixtureService {

    private final MatchService   matchService;
    private final TeamRepository teamRepository;
    private final NotificationService notificationService;

    @Autowired
    public FixtureService(MatchService matchService,
                          TeamRepository teamRepository,
                          NotificationService notificationService) {
        this.matchService        = matchService;
        this.teamRepository      = teamRepository;
        this.notificationService = notificationService;
    }

    /**
     * Generate Round Robin fixtures.
     * Every team plays every other team exactly once.
     * n teams → n*(n-1)/2 matches.
     */
    public List<Map<String, Object>> generateRoundRobin(String matchType, String venue, LocalDate startDate) {
        List<Team> teams = teamRepository.findAll();
        if (teams.size() < 2) throw new RuntimeException("Need at least 2 teams to generate fixtures.");

        List<Map<String, Object>> fixtures = new ArrayList<>();
        int dayOffset = 0;

        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                CricketMatch match = new CricketMatch();
                match.setTeam1(teams.get(i));
                match.setTeam2(teams.get(j));
                match.setMatchType(matchType != null ? matchType : "T20");
                match.setVenue(venue != null ? venue : "Auto-Generated Venue");
                match.setMatchDate(startDate != null ? startDate.plusDays(dayOffset) : LocalDate.now().plusDays(dayOffset));
                match.setStatus("Scheduled");

                CricketMatch saved = matchService.saveMatch(match);

                Map<String, Object> info = new LinkedHashMap<>();
                info.put("matchId", saved.getId());
                info.put("team1", teams.get(i).getTeamName());
                info.put("team2", teams.get(j).getTeamName());
                info.put("date", saved.getMatchDate().toString());
                info.put("round", "Round Robin");
                fixtures.add(info);

                dayOffset++;
            }
        }

        notificationService.createNotification(
            "🗓 Round Robin fixtures generated: " + fixtures.size() + " matches scheduled!",
            "FIXTURE", "🗓"
        );

        return fixtures;
    }

    /**
     * Generate Knockout (single-elimination) fixtures.
     * Only works cleanly with power-of-2 team counts (2, 4, 8, 16).
     * For non-power-of-2, some teams get byes.
     */
    public List<Map<String, Object>> generateKnockout(String matchType, String venue, LocalDate startDate) {
        List<Team> teams = new ArrayList<>(teamRepository.findAll());
        if (teams.size() < 2) throw new RuntimeException("Need at least 2 teams for knockout.");

        Collections.shuffle(teams); // Random seeding

        List<Map<String, Object>> fixtures = new ArrayList<>();
        int round = 1;
        int dayOffset = 0;

        // Pad to next power of 2 with byes
        int size = 1;
        while (size < teams.size()) size *= 2;

        // First round matches
        List<String> roundNames = List.of("Round 1", "Quarter-Final", "Semi-Final", "Final");

        for (int i = 0; i < teams.size() - 1; i += 2) {
            Team t1 = teams.get(i);
            Team t2 = (i + 1 < teams.size()) ? teams.get(i + 1) : null;

            if (t2 == null) continue; // bye

            CricketMatch match = new CricketMatch();
            match.setTeam1(t1);
            match.setTeam2(t2);
            match.setMatchType(matchType != null ? matchType : "T20");
            match.setVenue(venue != null ? venue : "Auto-Generated Venue");
            match.setMatchDate(startDate != null ? startDate.plusDays(dayOffset) : LocalDate.now().plusDays(dayOffset));
            match.setStatus("Scheduled");

            CricketMatch saved = matchService.saveMatch(match);

            String roundName = round <= roundNames.size() ? roundNames.get(round - 1) : "Round " + round;

            Map<String, Object> info = new LinkedHashMap<>();
            info.put("matchId", saved.getId());
            info.put("team1", t1.getTeamName());
            info.put("team2", t2.getTeamName());
            info.put("date", saved.getMatchDate().toString());
            info.put("round", roundName);
            fixtures.add(info);

            dayOffset++;
        }

        notificationService.createNotification(
            "🏆 Knockout fixtures generated: " + fixtures.size() + " matches scheduled!",
            "FIXTURE", "🏆"
        );

        return fixtures;
    }
}
