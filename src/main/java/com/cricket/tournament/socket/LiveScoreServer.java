package com.cricket.tournament.socket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * CO4 — TCP Socket Server for real-time live score broadcasting.
 *
 * This class implements Runnable so it can run in a background
 * daemon thread (started from TournamentApplication.java).
 *
 * Architecture:
 *   - ServerSocket listens on port 0
 *   - Each connecting client gets its own LiveScoreClientHandler thread
 *   - All client handlers register themselves with ScoreBroadcaster
 *   - When admin posts an event, ScoreBroadcaster.broadcast() sends
 *     the message to every registered client handler
 *
 * CO1 Multithreading: new Thread per client (see accept loop below)
 */
@Component
public class LiveScoreServer implements Runnable {

    @Value("${cricket.socket.port:0}")
    private int port;

    private final ScoreBroadcaster scoreBroadcaster;

    // Spring injects ScoreBroadcaster (singleton bean)
    public LiveScoreServer(ScoreBroadcaster scoreBroadcaster) {
        this.scoreBroadcaster = scoreBroadcaster;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("═══════════════════════════════════════════");
            System.out.println("  🏏 LiveScore Socket Server STARTED");
            System.out.println("  Listening on port: " + port);
            System.out.println("  Waiting for client connections...");
            System.out.println("═══════════════════════════════════════════");

            // ── Main accept loop ─────────────────────────────────────────
            // This loop runs forever, accepting new client connections.
            // Each client gets its own handler thread — CO1 Multithreading.
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Blocks here until a client connects
                    Socket clientSocket = serverSocket.accept();

                    System.out.println("[Socket] New client connected: "
                            + clientSocket.getInetAddress().getHostAddress()
                            + ":" + clientSocket.getPort());

                    // Create a handler for this specific client
                    LiveScoreClientHandler handler =
                            new LiveScoreClientHandler(clientSocket, scoreBroadcaster);

                    // Register with broadcaster so it can receive messages
                    scoreBroadcaster.registerClient(handler);

                    // CO1 — Spawn a new Thread for this client
                    // Thread name includes client info for easier debugging
                    Thread clientThread = new Thread(
                        handler,
                        "ClientHandler-" + clientSocket.getPort()
                    );
                    clientThread.setDaemon(true);
                    clientThread.start();

                    System.out.println("[Socket] Active clients: "
                            + scoreBroadcaster.getClientCount());

                } catch (IOException e) {
                    // Individual client accept failure — log and continue
                    // Don't kill the whole server for one bad connection
                    System.err.println("[Socket] Error accepting client: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("[Socket] FATAL: Could not start socket server on port "
                    + port + " — " + e.getMessage());
            System.err.println("[Socket] Check if port " + port + " is already in use.");
        }
    }
}