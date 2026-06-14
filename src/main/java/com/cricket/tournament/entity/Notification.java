package com.cricket.tournament.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Feature 7: In-App Notification System
 * Represents an in-app alert/notification for users.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "notif_type", length = 50)
    private String type;               // MATCH_START, RESULT, PLAYER_UPDATE, AUCTION, FIXTURE

    @Column(length = 30)
    private String icon;               // emoji icon

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ─── Constructors ─────────────────────────
    public Notification() {}

    public Notification(String message, String type, String icon) {
        this.message = message;
        this.type = type;
        this.icon = icon;
        this.read = false;
    }

    // ─── Getters & Setters ────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
