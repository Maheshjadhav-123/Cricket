package com.cricket.tournament.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a cricket team.
 * Relationship: One Team → Many Players (OneToMany)
 */
@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Team name is required")
    @Column(name = "team_name", nullable = false, unique = true, length = 100)
    private String teamName;

    @Column(name = "coach_name", length = 100)
    private String coachName;

    @Column(name = "home_city", length = 100)
    private String homeCity;

    @Column(name = "founded_year")
    private Integer foundedYear;

    // ─── Auction Fields (Feature 4: IPL-Style Auction) ───
    @Column(name = "budget")
    private Double budget;             // Total budget in lakhs (e.g. 8000)

    @Column(name = "purse_remaining")
    private Double purseRemaining;     // Remaining purse after buying players

    /**
     * CO3 — Hibernate OneToMany
     * mappedBy = "team" → Player owns the FK column team_id
     * cascade ALL → saving team saves its players
     * JsonIgnore → prevents infinite recursion in JSON
     */
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Player> players = new ArrayList<>();

    // ─── Constructors ──────────────────────────────────
    public Team() {}

    public Team(String teamName, String coachName, String homeCity, Integer foundedYear) {
        this.teamName    = teamName;
        this.coachName   = coachName;
        this.homeCity    = homeCity;
        this.foundedYear = foundedYear;
    }

    // ─── Getters & Setters ─────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getCoachName() { return coachName; }
    public void setCoachName(String coachName) { this.coachName = coachName; }

    public String getHomeCity() { return homeCity; }
    public void setHomeCity(String homeCity) { this.homeCity = homeCity; }

    public Integer getFoundedYear() { return foundedYear; }
    public void setFoundedYear(Integer foundedYear) { this.foundedYear = foundedYear; }

    public Double getBudget() { return budget; }
    public void setBudget(Double budget) { this.budget = budget; }

    public Double getPurseRemaining() { return purseRemaining; }
    public void setPurseRemaining(Double purseRemaining) { this.purseRemaining = purseRemaining; }

    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }

    @Override
    public String toString() {
        return "Team{id=" + id + ", teamName='" + teamName + "'}";
    }
}