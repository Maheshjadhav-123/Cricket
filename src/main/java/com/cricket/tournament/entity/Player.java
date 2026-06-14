package com.cricket.tournament.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a cricket player belonging to a team.
 * Relationship: Many Players → One Team (ManyToOne)
 *
 * CO3 — Hibernate @ManyToOne with @JoinColumn
 */
@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Player name is required")
    @Column(name = "player_name", nullable = false, length = 100)
    private String playerName;

    @Column(length = 50)
    private String role;   // Batsman, Bowler, All-rounder, Wicket-keeper

    @Column(name = "jersey_number")
    private Integer jerseyNumber;

    @Min(value = 1, message = "Age must be positive")
    @Column
    private Integer age;

    @Column(length = 50)
    private String nationality;

    // ─── Auction Fields (Feature 4: IPL-Style Auction) ───
    @Column(name = "base_price")
    private Double basePrice;          // Starting auction price in lakhs

    @Column(name = "sold_price")
    private Double soldPrice;          // Final sold price in lakhs

    @Column(name = "auction_status", length = 20)
    private String auctionStatus;      // UNSOLD, SOLD, RETAINED

    /**
     * CO3 — Hibernate ManyToOne
     * @JoinColumn creates FK column 'team_id' in players table
     * @JsonIgnoreProperties prevents infinite JSON recursion
     */
    @NotNull(message = "Team is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id", nullable = false)
    @JsonIgnoreProperties({"players", "hibernateLazyInitializer", "handler"})
    private Team team;

    // ─── Constructors ──────────────────────────────────
    public Player() {}

    public Player(String playerName, String role, Integer jerseyNumber,
                  Integer age, String nationality, Team team) {
        this.playerName   = playerName;
        this.role         = role;
        this.jerseyNumber = jerseyNumber;
        this.age          = age;
        this.nationality  = nationality;
        this.team         = team;
    }

    // ─── Getters & Setters ─────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getJerseyNumber() { return jerseyNumber; }
    public void setJerseyNumber(Integer jerseyNumber) { this.jerseyNumber = jerseyNumber; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public Double getBasePrice() { return basePrice; }
    public void setBasePrice(Double basePrice) { this.basePrice = basePrice; }

    public Double getSoldPrice() { return soldPrice; }
    public void setSoldPrice(Double soldPrice) { this.soldPrice = soldPrice; }

    public String getAuctionStatus() { return auctionStatus; }
    public void setAuctionStatus(String auctionStatus) { this.auctionStatus = auctionStatus; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    @Override
    public String toString() {
        return "Player{id=" + id + ", name='" + playerName + "', teamId="
               + (team != null ? team.getId() : null) + "}";
    }
}