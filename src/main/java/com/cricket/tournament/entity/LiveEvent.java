package com.cricket.tournament.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a real-time match event (boundary, wicket, six, etc.)
 * These are stored in DB AND broadcast via socket to all live viewers.
 *
 * Table: live_events
 * Relationship: ManyToOne → CricketMatch
 */
@Entity
@Table(name = "live_events")
public class LiveEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    @JsonIgnoreProperties({"liveEvents", "team1", "team2", "hibernateLazyInitializer"})
    private CricketMatch match;

    @Column(name = "event_type", length = 50)
    private String eventType;  // BOUNDARY, WICKET, SIX, OVER_COMPLETE, MATCH_START, MATCH_END

    @Column(length = 500)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ─── Constructors ─────────────────────────
    public LiveEvent() {}

    public LiveEvent(CricketMatch match, String eventType, String description) {
        this.match = match;
        this.eventType = eventType;
        this.description = description;
    }

    // ─── Getters & Setters ────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CricketMatch getMatch() { return match; }
    public void setMatch(CricketMatch match) { this.match = match; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Converts this event to a broadcast message string
     * sent through the socket to all connected clients.
     */
    public String toBroadcastMessage() {
        return "[" + eventType + "] " + description +
               " | Match#" + (match != null ? match.getId() : "?") +
               " | " + createdAt;
    }
}