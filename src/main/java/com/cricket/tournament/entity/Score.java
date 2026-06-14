package com.cricket.tournament.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Represents the scorecard for one team in one match inning.
 *
 * Relationships (CO3 — Hibernate):
 *   match → ManyToOne → CricketMatch  (FK: match_id)
 *   team  → ManyToOne → Team          (FK: team_id)
 */
@Entity
@Table(name = "scores")
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Which match this score entry belongs to.
     * FK column: match_id in scores table.
     */
    @NotNull(message = "Match is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "match_id", nullable = false)
    @JsonIgnoreProperties({"liveEvents", "hibernateLazyInitializer", "handler"})
    private CricketMatch match;

    /**
     * Which team's scorecard this represents.
     * FK column: team_id in scores table.
     */
    @NotNull(message = "Team is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id", nullable = false)
    @JsonIgnoreProperties({"players", "hibernateLazyInitializer", "handler"})
    private Team team;

    @Min(value = 0, message = "Runs cannot be negative")
    @Column(name = "runs_scored")
    private Integer runsScored;

    @Min(value = 0, message = "Wickets cannot be negative")
    @Column(name = "wickets_lost")
    private Integer wicketsLost;

    @Column(name = "overs_played")
    private Double oversPlayed;

    @Column
    private Integer extras;

    @Column(name = "inning_number")
    private Integer inningNumber;   // 1 or 2

    // ─── Constructors ──────────────────────────────────
    public Score() {}

    public Score(CricketMatch match, Team team, Integer runsScored,
                 Integer wicketsLost, Double oversPlayed,
                 Integer extras, Integer inningNumber) {
        this.match        = match;
        this.team         = team;
        this.runsScored   = runsScored;
        this.wicketsLost  = wicketsLost;
        this.oversPlayed  = oversPlayed;
        this.extras       = extras;
        this.inningNumber = inningNumber;
    }

    // ─── Getters & Setters ─────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CricketMatch getMatch() { return match; }
    public void setMatch(CricketMatch match) { this.match = match; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public Integer getRunsScored() { return runsScored; }
    public void setRunsScored(Integer runsScored) { this.runsScored = runsScored; }

    public Integer getWicketsLost() { return wicketsLost; }
    public void setWicketsLost(Integer wicketsLost) { this.wicketsLost = wicketsLost; }

    public Double getOversPlayed() { return oversPlayed; }
    public void setOversPlayed(Double oversPlayed) { this.oversPlayed = oversPlayed; }

    public Integer getExtras() { return extras; }
    public void setExtras(Integer extras) { this.extras = extras; }

    public Integer getInningNumber() { return inningNumber; }
    public void setInningNumber(Integer inningNumber) { this.inningNumber = inningNumber; }

    @Override
    public String toString() {
        return "Score{matchId=" + (match != null ? match.getId() : null)
               + ", team=" + (team != null ? team.getTeamName() : null)
               + ", " + runsScored + "/" + wicketsLost + "}";
    }
}