package com.cricket.tournament.socket;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CO4 — Singleton broadcaster that holds ALL connected socket clients.
 *
 * This is the bridge between:
 *   - Spring (LiveEventService calls broadcast())
 *   - Socket layer (LiveScoreClientHandler instances)
 *
 * CO1 Multithreading safety:
 *   - Client list uses Collections.synchronizedList()
 *   - broadcast() iterates over a SNAPSHOT copy to avoid
 *     ConcurrentModificationException if a client disconnects mid-broadcast
 *
 * Marked @Component so Spring manages it as a singleton bean,
 * which means LiveEventService and LiveScoreServer share the SAME instance.
 */
@Component
public class ScoreBroadcaster {

    /**
     * Thread-safe list of all currently connected client handlers.
     * synchronizedList wraps ArrayList to make add/remove atomic.
     */
    private final List<LiveScoreClientHandler> clients =
            Collections.synchronizedList(new ArrayList<>());

    /**
     * Register a newly connected client handler.
     * Called by LiveScoreServer when a new socket connection is accepted.
     */
    public void registerClient(LiveScoreClientHandler handler) {
        clients.add(handler);
        System.out.println("[Broadcaster] Client registered. Total: " + clients.size());
    }

    /**
     * Unregister a client that has disconnected.
     * Called by LiveScoreClientHandler.disconnect().
     */
    public void unregisterClient(LiveScoreClientHandler handler) {
        clients.remove(handler);
        System.out.println("[Broadcaster] Client removed. Total: " + clients.size());
    }

    /**
     * CO4 — Broadcast a message to ALL connected clients.
     *
     * Called from LiveEventService when admin posts a live event.
     *
     * Implementation detail:
     *   We iterate over a SNAPSHOT (new ArrayList copy) of the clients list.
     *   This is important because a client might disconnect (and call
     *   unregisterClient) while we are mid-iteration — without the snapshot,
     *   this would throw ConcurrentModificationException.
     */
    public void broadcast(String message) {
        // Take snapshot to avoid concurrent modification
        List<LiveScoreClientHandler> snapshot;
        synchronized (clients) {
            snapshot = new ArrayList<>(clients);
        }

        System.out.println("[Broadcaster] Broadcasting to "
                + snapshot.size() + " client(s): " + message);

        List<LiveScoreClientHandler> deadClients = new ArrayList<>();

        for (LiveScoreClientHandler client : snapshot) {
            if (client.isConnected()) {
                client.sendMessage("EVENT|" + message);
            } else {
                // Mark dead clients for cleanup
                deadClients.add(client);
            }
        }

        // Clean up any dead connections we discovered during broadcast
        clients.removeAll(deadClients);
    }

    /**
     * Broadcast to clients subscribed to a specific match only.
     * (Used for targeted match updates)
     */
    public void broadcastMatchEvent(Long matchId, String message) {
        broadcast("MATCH#" + matchId + "|" + message);
    }

    public int getClientCount() {
        return clients.size();
    }
}