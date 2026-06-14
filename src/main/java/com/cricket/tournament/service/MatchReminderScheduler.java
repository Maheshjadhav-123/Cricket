package com.cricket.tournament.service;

import com.cricket.tournament.entity.CricketMatch;
import com.cricket.tournament.repository.MatchRepository;
import com.cricket.tournament.socket.ScoreBroadcaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * CO1 — Multithreading via Spring @Scheduled.
 *
 * @Scheduled methods run on a background daemon thread managed by
 * Spring's task scheduler (pool size in application.properties).
 *
 * This scheduler runs automatically — no HTTP request needed.
 * It demonstrates background thread execution independent of user actions.
 *
 * Enabled by @EnableScheduling in TournamentApplication.java.
 */
@Service
public class MatchReminderScheduler {

    private final MatchRepository matchRepository;
    private final ScoreBroadcaster scoreBroadcaster;

    @Autowired
    public MatchReminderScheduler(MatchRepository matchRepository,
                                  ScoreBroadcaster scoreBroadcaster) {
        this.matchRepository  = matchRepository;
        this.scoreBroadcaster = scoreBroadcaster;
    }

    /**
     * CO1 Multithreading — Runs every 60 seconds on a scheduler thread.
     *
     * Checks for matches scheduled TODAY and broadcasts reminders
     * to all connected socket clients.
     *
     * fixedDelay = 60000 means: 60 seconds after last execution ends.
     */
    @Scheduled(fixedDelay = 60000)
    public void sendMatchReminders() {
        String threadName = Thread.currentThread().getName();
        System.out.println("[Scheduler] Running match reminder check. Thread: " + threadName);

        try {
            List<CricketMatch> todaysMatches =
                matchRepository.findAll().stream()
                    .filter(m -> LocalDate.now().equals(m.getMatchDate()))
                    .filter(m -> "Scheduled".equalsIgnoreCase(m.getStatus()))
                    .toList();

            if (todaysMatches.isEmpty()) {
                System.out.println("[Scheduler] No matches scheduled for today.");
                return;
            }

            for (CricketMatch match : todaysMatches) {
                String reminder = "REMINDER|Match Today: "
                        + match.getTeam1().getTeamName()
                        + " vs "
                        + match.getTeam2().getTeamName()
                        + " at " + match.getVenue()
                        + " | Type: " + match.getMatchType();

                // Broadcast reminder to all connected socket clients
                scoreBroadcaster.broadcast(reminder);

                System.out.println("[Scheduler] Reminder sent for Match #" + match.getId()
                        + " on thread: " + threadName);
            }

        } catch (Exception e) {
            System.err.println("[Scheduler] Error in reminder task: " + e.getMessage());
        }
    }

    /**
     * CO1 Multithreading — Runs every day at midnight (cron expression).
     * Automatically marks past "Scheduled" matches as "Completed".
     *
     * cron = "0 0 0 * * *" → second=0, minute=0, hour=0, every day
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void autoCompleteOldMatches() {
        String threadName = Thread.currentThread().getName();
        System.out.println("[Scheduler] Running auto-complete check. Thread: " + threadName);

        List<CricketMatch> pastMatches =
            matchRepository.findAll().stream()
                .filter(m -> m.getMatchDate() != null
                          && m.getMatchDate().isBefore(LocalDate.now())
                          && "Scheduled".equalsIgnoreCase(m.getStatus()))
                .toList();

        for (CricketMatch match : pastMatches) {
            match.setStatus("Completed");
            matchRepository.save(match);
            System.out.println("[Scheduler] Auto-completed match #" + match.getId());
        }

        System.out.println("[Scheduler] Auto-complete done. "
                + pastMatches.size() + " matches updated.");
    }
}