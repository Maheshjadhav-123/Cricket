package com.cricket.tournament.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a cricket match.
 * Table: cricket_matches (avoids MySQL reserved keyword "match")
 *
 * Relationships:
 *   team1, team2   → ManyToOne → teams
 *   liveEvents     → OneToMany → live_events
 */
@Entity
@Table(name = "cricket_matches")
public class CricketMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team1_id", nullable = false)
    @JsonIgnoreProperties({"players", "hibernateLazyInitializer", "handler"})
    private Team team1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team2_id", nullable = false)
    @JsonIgnoreProperties({"players", "hibernateLazyInitializer", "handler"})
    private Team team2;

    @Column(length = 150)
    private String venue;

    @Column(name = "match_date")
    private LocalDate matchDate;

    @Column(name = "match_type", length = 50)
    private String matchType;   // T20, ODI, Test

    @Column(length = 50)
    private String status;      // Scheduled, Live, Completed, Cancelled

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"match"})
    private List<LiveEvent> liveEvents = new ArrayList<>();

    // ─── Constructors ─────────────────────────
    public CricketMatch() {}

    // ─── Getters & Setters ────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Team getTeam1() { return team1; }
    public void setTeam1(Team team1) { this.team1 = team1; }

    public Team getTeam2() { return team2; }
    public void setTeam2(Team team2) { this.team2 = team2; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public LocalDate getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDate matchDate) { this.matchDate = matchDate; }

    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<LiveEvent> getLiveEvents() { return liveEvents; }
    public void setLiveEvents(List<LiveEvent> liveEvents) { this.liveEvents = liveEvents; }
}