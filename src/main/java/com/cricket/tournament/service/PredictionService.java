package com.cricket.tournament.service;

import com.cricket.tournament.jdbc.JdbcReportDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Feature 1: AI-Based Match Prediction Service
 *
 * Uses historical data to predict match outcomes:
 *   - Team win ratio (wins ÷ total matches played)
 *   - Average runs scored per match
 *   - Head-to-head record between two teams
 *
 * Viva line: "System uses historical data to predict match outcomes
 *             and assist users in decision-making."
 */
@Service
public class PredictionService {

    private final JdbcReportDao jdbcReportDao;

    @Autowired
    public PredictionService(JdbcReportDao jdbcReportDao) {
        this.jdbcReportDao = jdbcReportDao;
    }

    /**
     * Predict match outcome between two teams.
     * Returns a map with win probabilities and analysis.
     */
    public Map<String, Object> predictMatch(Long team1Id, Long team2Id) {
        Map<String, Object> result = new LinkedHashMap<>();

        // Get historical stats for both teams
        Map<String, Object> stats1 = jdbcReportDao.getTeamPredictionStats(team1Id);
        Map<String, Object> stats2 = jdbcReportDao.getTeamPredictionStats(team2Id);

        String team1Name = (String) stats1.getOrDefault("team_name", "Team 1");
        String team2Name = (String) stats2.getOrDefault("team_name", "Team 2");

        // Calculate win ratios
        double wins1    = toDouble(stats1.get("wins"));
        double total1   = toDouble(stats1.get("total_matches"));
        double avgRuns1 = toDouble(stats1.get("avg_runs"));

        double wins2    = toDouble(stats2.get("wins"));
        double total2   = toDouble(stats2.get("total_matches"));
        double avgRuns2 = toDouble(stats2.get("avg_runs"));

        // Win ratio component (40% weight)
        double winRatio1 = total1 > 0 ? wins1 / total1 : 0.5;
        double winRatio2 = total2 > 0 ? wins2 / total2 : 0.5;

        // Average runs component (30% weight) — normalized
        double maxRuns = Math.max(avgRuns1, avgRuns2);
        double runsScore1 = maxRuns > 0 ? avgRuns1 / maxRuns : 0.5;
        double runsScore2 = maxRuns > 0 ? avgRuns2 / maxRuns : 0.5;

        // Head-to-head component (30% weight)
        Map<String, Object> h2h = jdbcReportDao.getHeadToHead(team1Id, team2Id);
        double h2hWins1 = toDouble(h2h.get("team1_wins"));
        double h2hWins2 = toDouble(h2h.get("team2_wins"));
        double h2hTotal = h2hWins1 + h2hWins2;
        double h2hScore1 = h2hTotal > 0 ? h2hWins1 / h2hTotal : 0.5;
        double h2hScore2 = h2hTotal > 0 ? h2hWins2 / h2hTotal : 0.5;

        // Weighted composite score
        double score1 = (winRatio1 * 0.4) + (runsScore1 * 0.3) + (h2hScore1 * 0.3);
        double score2 = (winRatio2 * 0.4) + (runsScore2 * 0.3) + (h2hScore2 * 0.3);

        // Convert to probabilities
        double totalScore = score1 + score2;
        double prob1 = totalScore > 0 ? Math.round(score1 / totalScore * 1000.0) / 10.0 : 50.0;
        double prob2 = totalScore > 0 ? Math.round(score2 / totalScore * 1000.0) / 10.0 : 50.0;

        // Build result
        result.put("team1_name", team1Name);
        result.put("team2_name", team2Name);
        result.put("team1_win_probability", prob1);
        result.put("team2_win_probability", prob2);
        result.put("predicted_winner", prob1 >= prob2 ? team1Name : team2Name);
        result.put("confidence", Math.abs(prob1 - prob2) > 20 ? "HIGH" :
                                 Math.abs(prob1 - prob2) > 10 ? "MEDIUM" : "LOW");

        // Breakdown for display
        Map<String, Object> analysis = new LinkedHashMap<>();
        analysis.put("team1_win_ratio", Math.round(winRatio1 * 100) + "%");
        analysis.put("team2_win_ratio", Math.round(winRatio2 * 100) + "%");
        analysis.put("team1_avg_runs", Math.round(avgRuns1));
        analysis.put("team2_avg_runs", Math.round(avgRuns2));
        analysis.put("head_to_head", h2hWins1 + " - " + h2hWins2);
        analysis.put("team1_matches_played", (int) total1);
        analysis.put("team2_matches_played", (int) total2);

        result.put("analysis", analysis);
        result.put("algorithm", "Weighted composite: Win Ratio (40%) + Avg Runs (30%) + Head-to-Head (30%)");

        return result;
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }
}
