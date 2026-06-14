package com.cricket.tournament;

import com.cricket.tournament.socket.LiveScoreServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CricketPro Enterprise — Main Application Entry Point
 *
 * @EnableAsync     → Enables CO1 Multithreading via @Async methods
 * @EnableScheduling → Enables CO1 @Scheduled background tasks
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class TournamentApplication {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(TournamentApplication.class, args);

        // CO4: Start Socket Server in a background thread
        // so it doesn't block Spring Boot startup
       // LiveScoreServer socketServer = ctx.getBean(LiveScoreServer.class);
        //Thread socketThread = new Thread(socketServer, "LiveScoreSocket-Main");
        //socketThread.setDaemon(true);   // dies when JVM exits
        //socketThread.start();

        System.out.println("✅ CricketPro Enterprise started on port 8082");
        System.out.println("✅ Live Score Socket Server started on port 9090");
    }
}