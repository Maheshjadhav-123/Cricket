package com.cricket.tournament.service;

import com.cricket.tournament.entity.CricketMatch;
import com.cricket.tournament.entity.LiveEvent;
import com.cricket.tournament.repository.LiveEventRepository;
import com.cricket.tournament.repository.MatchRepository;
import com.cricket.tournament.socket.ScoreBroadcaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * CO3 + CO4 — Service for live match events.
 *
 * When admin posts a live event:
 *  1. Saves it to DB (JPA)
 *  2. Broadcasts it to all socket-connected clients (CO4)
 */
@Service
@Transactional
public class LiveEventService {

    private final LiveEventRepository liveEventRepository;
    private final MatchRepository     matchRepository;
    private final ScoreBroadcaster    scoreBroadcaster;

    @Autowired
    public LiveEventService(LiveEventRepository liveEventRepository,
                            MatchRepository matchRepository,
                            ScoreBroadcaster scoreBroadcaster) {
        this.liveEventRepository = liveEventRepository;
        this.matchRepository     = matchRepository;
        this.scoreBroadcaster    = scoreBroadcaster;
    }

    /**
     * Post a new live event for a match.
     * Saves to DB then broadcasts via socket to all viewers.
     */
    public LiveEvent postEvent(Long matchId, String eventType, String description) {
        CricketMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found: " + matchId));

        LiveEvent event = new LiveEvent(match, eventType, description);
        LiveEvent saved = liveEventRepository.save(event);

        // CO4 — Broadcast this event through the socket server to all clients
        scoreBroadcaster.broadcast(saved.toBroadcastMessage());

        return saved;
    }

    @Transactional(readOnly = true)
    public List<LiveEvent> getRecentEvents(Long matchId) {
        return liveEventRepository.findTop10ByMatchIdOrderByCreatedAtDesc(matchId);
    }

    @Transactional(readOnly = true)
    public List<LiveEvent> getAllEventsForMatch(Long matchId) {
        return liveEventRepository.findByMatchIdOrderByCreatedAtDesc(matchId);
    }
}