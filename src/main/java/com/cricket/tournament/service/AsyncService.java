package com.cricket.tournament.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncService {
    
    @Async
    public void logAsyncEvent(String message) {
        System.out.println("[ASYNC BACKGROUND TASK] Thread: " + Thread.currentThread().getName() + " - " + message);
        try {
            Thread.sleep(1000); // Simulate background work like sending notification
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
