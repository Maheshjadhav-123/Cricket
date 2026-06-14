package com.cricket.tournament.socket;

import java.io.*;
import java.net.Socket;

/**
 * CO4 — Handles communication with ONE connected socket client.
 * CO1 — Implements Runnable; each instance runs in its own Thread.
 *
 * Lifecycle:
 *   1. Client connects → LiveScoreServer creates this handler
 *   2. Handler is registered with ScoreBroadcaster
 *   3. run() loop reads any messages the client sends (keep-alive / commands)
 *   4. sendMessage() is called by ScoreBroadcaster to push events to client
 *   5. On disconnect → unregisters from ScoreBroadcaster and closes socket
 */
public class LiveScoreClientHandler implements Runnable {

    private final Socket           clientSocket;
    private final ScoreBroadcaster broadcaster;
    private       PrintWriter      out;
    private       BufferedReader   in;
    private       boolean          connected = true;

    public LiveScoreClientHandler(Socket clientSocket, ScoreBroadcaster broadcaster) {
        this.clientSocket = clientSocket;
        this.broadcaster  = broadcaster;
    }

    @Override
    public void run() {
        try {
            // Set up I/O streams for this client
            out = new PrintWriter(
                    new BufferedWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream())), true);
            in  = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

            // ── Send welcome message to newly connected client ──────────
            sendMessage("CONNECTED|Welcome to CricketPro Live Score Server!");
            sendMessage("INFO|You will receive live match updates in real-time.");

            // ── Read loop: listen for client messages ───────────────────
            // Clients can send: SUBSCRIBE:<matchId>, PING, DISCONNECT
            String clientMessage;
            while (connected && (clientMessage = in.readLine()) != null) {

                System.out.println("[ClientHandler-" + clientSocket.getPort()
                        + "] Received: " + clientMessage);

                if (clientMessage.trim().equalsIgnoreCase("PING")) {
                    // Heartbeat — client checking connection is alive
                    sendMessage("PONG|Server is alive");

                } else if (clientMessage.startsWith("SUBSCRIBE:")) {
                    // Client subscribing to a specific match feed
                    String matchId = clientMessage.split(":")[1].trim();
                    sendMessage("SUBSCRIBED|Now receiving live updates for Match #" + matchId);

                } else if (clientMessage.equalsIgnoreCase("DISCONNECT")) {
                    // Client gracefully disconnecting
                    sendMessage("BYE|Disconnecting. Thanks for watching!");
                    break;

                } else {
                    // Echo unknown commands back
                    sendMessage("UNKNOWN|Command not recognised: " + clientMessage);
                }
            }

        } catch (IOException e) {
            // Client disconnected abruptly (browser closed, network drop, etc.)
            System.out.println("[ClientHandler] Client disconnected abruptly: "
                    + clientSocket.getPort());
        } finally {
            disconnect();
        }
    }

    /**
     * Called by ScoreBroadcaster to push a message to THIS specific client.
     * Thread-safe: PrintWriter with autoFlush=true.
     */
    public void sendMessage(String message) {
        if (out != null && connected && !clientSocket.isClosed()) {
            out.println(message);   // println auto-flushes (autoFlush=true in constructor)
        }
    }

    /**
     * Clean up resources and unregister from broadcaster.
     */
    public void disconnect() {
        connected = false;
        broadcaster.unregisterClient(this);

        try {
            if (out  != null) out.close();
            if (in   != null) in.close();
            if (!clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.err.println("[ClientHandler] Error closing socket: " + e.getMessage());
        }

        System.out.println("[Socket] Client disconnected. Active clients: "
                + broadcaster.getClientCount());
    }

    public boolean isConnected() {
        return connected && !clientSocket.isClosed();
    }

    public String getClientAddress() {
        return clientSocket.getInetAddress().getHostAddress()
               + ":" + clientSocket.getPort();
    }
}