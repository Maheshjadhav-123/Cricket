package com.cricket.tournament.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * CO1 — Raw JDBC Data Access Object for complex analytical queries.
 *
 * WHY JDBC instead of JPA here?
 * These queries involve multi-table JOINs, GROUP BY aggregations,
 * and computed columns (total_runs, win_count, etc.) that JPA
 * cannot express cleanly. Raw JDBC gives us:
 *   1. Full SQL control
 *   2. Better performance for reporting
 *   3. No N+1 query problem
 *
 * JdbcTemplate is Spring's wrapper around raw JDBC that:
 *   - Handles connection open/close automatically
 *   - Converts ResultSet rows into List<Map<String,Object>>
 *   - Prevents resource leaks
 */
@Repository
public class JdbcReportDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcReportDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ══════════════════════════════════════════════════════════════
    // REPORT 1: Top 5 Run Scorers across all matches
    // ══════════════════════════════════════════════════════════════

    /**
     * Raw JDBC query — Multi-table JOIN with GROUP BY and ORDER BY.
     * Returns: playerName, teamName, totalRuns (aggregated)
     */
    public List<Map<String, Object>> getTopRunScorers() {
        String sql =
            "SELECT p.player_name, t.team_name, SUM(s.runs_scored) AS total_runs " +
            "FROM scores s " +
            "JOIN teams  t ON s.team_id  = t.id " +
            "JOIN players p ON p.team_id  = t.id " +
            "GROUP BY p.player_name, t.team_name " +
            "ORDER BY total_runs DESC " +
            "LIMIT 5";

        return jdbcTemplate.queryForList(sql);
    }

    // ══════════════════════════════════════════════════════════════
    // REPORT 2: Team Win/Loss Ratio
    // (A team "wins" if their runs_scored > opponent runs in same match)
    // ══════════════════════════════════════════════════════════════

    /**
     * Raw JDBC — Correlated subquery to compute win counts per team.
     */
    public List<Map<String, Object>> getTeamWinLossStats() {
        String sql =
            "SELECT " +
            "  t.team_name, " +
            "  COUNT(DISTINCT m.id) AS total_matches, " +
            "  SUM(CASE WHEN s1.runs_scored > s2.runs_scored THEN 1 ELSE 0 END) AS wins, " +
            "  SUM(CASE WHEN s1.runs_scored < s2.runs_scored THEN 1 ELSE 0 END) AS losses, " +
            "  SUM(CASE WHEN s1.runs_scored = s2.runs_scored THEN 1 ELSE 0 END) AS draws " +
            "FROM teams t " +
            "LEFT JOIN scores s1 ON s1.team_id = t.id " +
            "LEFT JOIN cricket_matches m " +
            "       ON (m.team1_id = t.id OR m.team2_id = t.id) " +
            "       AND (m.team1_id = s1.match_id OR m.team2_id = s1.match_id) " +
            "LEFT JOIN scores s2 " +
            "       ON s2.match_id = s1.match_id AND s2.team_id != t.id " +
            "GROUP BY t.team_name " +
            "ORDER BY wins DESC";

        return jdbcTemplate.queryForList(sql);
    }

    // ══════════════════════════════════════════════════════════════
    // REPORT 3: Match frequency by month (for SDG dashboard chart)
    // ══════════════════════════════════════════════════════════════

    /**
     * Raw JDBC — DATE_FORMAT grouping for monthly analytics.
     */
    public List<Map<String, Object>> getMatchesPerMonth() {
        String sql =
            "SELECT " +
            "  DATE_FORMAT(match_date, '%Y-%m') AS month, " +
            "  COUNT(*) AS match_count " +
            "FROM cricket_matches " +
            "WHERE match_date IS NOT NULL " +
            "GROUP BY DATE_FORMAT(match_date, '%Y-%m') " +
            "ORDER BY month ASC";

        return jdbcTemplate.queryForList(sql);
    }

    // ══════════════════════════════════════════════════════════════
    // REPORT 4: Dashboard summary stats (single query, fast)
    // ══════════════════════════════════════════════════════════════

    /**
     * Raw JDBC — Returns aggregate counts for dashboard tiles.
     * One query instead of 4 separate JPA calls.
     */
    public Map<String, Object> getDashboardStats() {
        String sql =
            "SELECT " +
            "  (SELECT COUNT(*) FROM teams)           AS total_teams, " +
            "  (SELECT COUNT(*) FROM players)         AS total_players, " +
            "  (SELECT COUNT(*) FROM cricket_matches) AS total_matches, " +
            "  (SELECT COUNT(*) FROM cricket_matches WHERE status = 'Live') AS live_matches, " +
            "  (SELECT COUNT(*) FROM users WHERE approved = true) AS active_users";

        return jdbcTemplate.queryForMap(sql);
    }

    // ══════════════════════════════════════════════════════════════
    // REPORT 5: SDG Metric — Youth player participation by city
    // ══════════════════════════════════════════════════════════════

    /**
     * Raw JDBC — SDG impact metric.
     * Counts players grouped by their team's home city.
     * Used in the SDG dashboard to show regional reach.
     */
    public List<Map<String, Object>> getPlayersByCity() {
        String sql =
            "SELECT t.home_city, COUNT(p.id) AS player_count " +
            "FROM teams t " +
            "LEFT JOIN players p ON p.team_id = t.id " +
            "WHERE t.home_city IS NOT NULL " +
            "GROUP BY t.home_city " +
            "ORDER BY player_count DESC";

        return jdbcTemplate.queryForList(sql);
    }

    // ══════════════════════════════════════════════════════════════
    // REPORT 6: Highest scoring match (for highlights)
    // ══════════════════════════════════════════════════════════════

    public List<Map<String, Object>> getHighestScoringMatches() {
        String sql =
            "SELECT " +
            "  m.id AS match_id, " +
            "  t1.team_name AS team1, " +
            "  t2.team_name AS team2, " +
            "  m.venue, " +
            "  m.match_date, " +
            "  SUM(s.runs_scored) AS total_runs_in_match " +
            "FROM cricket_matches m " +
            "JOIN teams  t1 ON m.team1_id = t1.id " +
            "JOIN teams  t2 ON m.team2_id = t2.id " +
            "LEFT JOIN scores s ON s.match_id = m.id " +
            "GROUP BY m.id, t1.team_name, t2.team_name, m.venue, m.match_date " +
            "ORDER BY total_runs_in_match DESC " +
            "LIMIT 5";

        return jdbcTemplate.queryForList(sql);
    }

    // ══════════════════════════════════════════════════════════════
    // FEATURE 1: AI Prediction — Team historical stats
    // ══════════════════════════════════════════════════════════════

    public Map<String, Object> getTeamPredictionStats(Long teamId) {
        String sql =
            "SELECT " +
            "  t.team_name, " +
            "  COALESCE(COUNT(DISTINCT s.match_id), 0) AS total_matches, " +
            "  COALESCE(AVG(s.runs_scored), 0)          AS avg_runs, " +
            "  COALESCE(SUM(CASE WHEN s.runs_scored > ( " +
            "    SELECT COALESCE(s2.runs_scored, 0) FROM scores s2 " +
            "    WHERE s2.match_id = s.match_id AND s2.team_id != t.id LIMIT 1 " +
            "  ) THEN 1 ELSE 0 END), 0) AS wins " +
            "FROM teams t " +
            "LEFT JOIN scores s ON s.team_id = t.id " +
            "WHERE t.id = ?  " +
            "GROUP BY t.team_name";

        try {
            return jdbcTemplate.queryForMap(sql, teamId);
        } catch (Exception e) {
            Map<String, Object> empty = new java.util.LinkedHashMap<>();
            empty.put("team_name", "Unknown");
            empty.put("total_matches", 0);
            empty.put("avg_runs", 0);
            empty.put("wins", 0);
            return empty;
        }
    }

    public Map<String, Object> getHeadToHead(Long team1Id, Long team2Id) {
        String sql =
            "SELECT " +
            "  COALESCE(SUM(CASE WHEN s1.runs_scored > s2.runs_scored THEN 1 ELSE 0 END), 0) AS team1_wins, " +
            "  COALESCE(SUM(CASE WHEN s2.runs_scored > s1.runs_scored THEN 1 ELSE 0 END), 0) AS team2_wins " +
            "FROM scores s1 " +
            "JOIN scores s2 ON s1.match_id = s2.match_id AND s1.team_id != s2.team_id " +
            "WHERE s1.team_id = ? AND s2.team_id = ?";

        try {
            return jdbcTemplate.queryForMap(sql, team1Id, team2Id);
        } catch (Exception e) {
            Map<String, Object> empty = new java.util.LinkedHashMap<>();
            empty.put("team1_wins", 0);
            empty.put("team2_wins", 0);
            return empty;
        }
    }

    // ══════════════════════════════════════════════════════════════
    // FEATURE 3: Player Performance Analytics
    // ══════════════════════════════════════════════════════════════

    public List<Map<String, Object>> getPlayerAnalytics() {
        String sql =
            "SELECT " +
            "  p.id AS player_id, " +
            "  p.player_name, " +
            "  p.role, " +
            "  t.team_name, " +
            "  COALESCE(SUM(s.runs_scored), 0) AS total_runs, " +
            "  COALESCE(COUNT(DISTINCT s.match_id), 0) AS matches_played, " +
            "  COALESCE(AVG(s.runs_scored), 0) AS avg_runs, " +
            "  COALESCE(SUM(s.wickets_lost), 0) AS total_wickets_faced " +
            "FROM players p " +
            "JOIN teams t ON p.team_id = t.id " +
            "LEFT JOIN scores s ON s.team_id = t.id " +
            "GROUP BY p.id, p.player_name, p.role, t.team_name " +
            "ORDER BY total_runs DESC";

        return jdbcTemplate.queryForList(sql);
    }

    // ══════════════════════════════════════════════════════════════
    // FEATURE 8: Leaderboard & Ranking
    // ══════════════════════════════════════════════════════════════

    /** Top batsmen by cumulative team runs */
    public List<Map<String, Object>> getTopBatsmen() {
        String sql =
            "SELECT " +
            "  t.team_name, " +
            "  SUM(s.runs_scored) AS total_runs, " +
            "  COUNT(DISTINCT s.match_id) AS matches, " +
            "  ROUND(AVG(s.runs_scored), 1) AS avg_score, " +
            "  MAX(s.runs_scored) AS highest_score " +
            "FROM scores s " +
            "JOIN teams t ON s.team_id = t.id " +
            "GROUP BY t.team_name " +
            "ORDER BY total_runs DESC " +
            "LIMIT 10";
        return jdbcTemplate.queryForList(sql);
    }

    /** Top bowlers (teams with lowest avg runs conceded against them) */
    public List<Map<String, Object>> getTopBowlers() {
        String sql =
            "SELECT " +
            "  t.team_name AS bowling_team, " +
            "  COUNT(DISTINCT s.match_id) AS matches_bowled, " +
            "  SUM(s.wickets_lost) AS total_wickets_taken, " +
            "  ROUND(AVG(s.runs_scored), 1) AS avg_runs_conceded " +
            "FROM scores s " +
            "JOIN cricket_matches m ON s.match_id = m.id " +
            "JOIN teams t ON (t.id = m.team1_id OR t.id = m.team2_id) AND t.id != s.team_id " +
            "GROUP BY t.team_name " +
            "ORDER BY total_wickets_taken DESC " +
            "LIMIT 10";
        return jdbcTemplate.queryForList(sql);
    }

    /** Best teams by win percentage */
    public List<Map<String, Object>> getTeamRankings() {
        String sql =
            "SELECT " +
            "  t.team_name, " +
            "  COUNT(DISTINCT s1.match_id) AS total_matches, " +
            "  COALESCE(SUM(CASE WHEN s1.runs_scored > s2.runs_scored THEN 1 ELSE 0 END), 0) AS wins, " +
            "  COALESCE(SUM(CASE WHEN s1.runs_scored < s2.runs_scored THEN 1 ELSE 0 END), 0) AS losses, " +
            "  ROUND(COALESCE(SUM(CASE WHEN s1.runs_scored > s2.runs_scored THEN 1 ELSE 0 END), 0) * 100.0 / " +
            "    GREATEST(COUNT(DISTINCT s1.match_id), 1), 1) AS win_pct " +
            "FROM teams t " +
            "LEFT JOIN scores s1 ON s1.team_id = t.id " +
            "LEFT JOIN scores s2 ON s2.match_id = s1.match_id AND s2.team_id != t.id " +
            "GROUP BY t.team_name " +
            "HAVING COUNT(DISTINCT s1.match_id) > 0 " +
            "ORDER BY win_pct DESC, wins DESC";
        return jdbcTemplate.queryForList(sql);
    }
}