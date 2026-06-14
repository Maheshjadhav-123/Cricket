package com.cricket.tournament.controller;

import com.cricket.tournament.entity.LiveEvent;
import com.cricket.tournament.service.LiveEventService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * CO4 — Live score REST controller.
 * Admin posts events; all users can view the live feed.
 */
@RestController
@RequestMapping("/api/live")
@CrossOrigin(origins = "*")
public class LiveScoreController {

    private final LiveEventService liveEventService;

    @Autowired
    public LiveScoreController(LiveEventService liveEventService) {
        this.liveEventService = liveEventService;
    }

    /**
     * POST /api/live/event
     * Admin posts a new live event (boundary, wicket, etc.)
     * This saves to DB AND broadcasts via socket to all connected viewers.
     */
    @PostMapping("/event")
    public ResponseEntity<?> postEvent(@RequestBody Map<String, String> body,
                                       HttpSession session) {
        // Only ADMIN can post live events
        Object role = session.getAttribute("userRole");
        if (!"ADMIN".equalsIgnoreCase(role != null ? role.toString() : "")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Admin access required to post live events."));
        }

        try {
            Long   matchId     = Long.parseLong(body.get("matchId"));
            String eventType   = body.get("eventType");
            String description = body.get("description");

            LiveEvent event = liveEventService.postEvent(matchId, eventType, description);
            return ResponseEntity.ok(Map.of(
                "message", "Live event posted and broadcast to all viewers.",
                "eventId", event.getId(),
                "broadcastMsg", event.toBroadcastMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/live/events/{matchId}
     * Returns last 10 events for a match (for UI polling fallback).
     */
    @GetMapping("/events/{matchId}")
    public ResponseEntity<List<LiveEvent>> getRecentEvents(@PathVariable Long matchId) {
        return ResponseEntity.ok(liveEventService.getRecentEvents(matchId));
    }
}