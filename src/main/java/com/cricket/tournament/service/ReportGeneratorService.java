package com.cricket.tournament.service;

import com.cricket.tournament.jdbc.JdbcReportDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CO1 — Multithreading via @Async and ExecutorService.
 *
 * @Async methods run on a separate thread from the thread pool
 * configured in application.properties:
 *   spring.task.execution.pool.core-size=4
 *   spring.task.execution.pool.max-size=10
 *
 * This means the HTTP request returns IMMEDIATELY with a task ID,
 * while the report builds in the background on a worker thread.
 *
 * Thread pool configured by @EnableAsync in TournamentApplication.
 */
@Service
public class ReportGeneratorService {

    private final JdbcReportDao jdbcReportDao;

    // In-memory task status store (production would use Redis/DB)
    private final ConcurrentHashMap<String, String> taskStatus = new ConcurrentHashMap<>();

    @Autowired
    public ReportGeneratorService(JdbcReportDao jdbcReportDao) {
        this.jdbcReportDao = jdbcReportDao;
    }

    /**
     * CO1 Multithreading — Launches async report generation.
     *
     * Returns a task ID immediately (non-blocking).
     * The actual report generation runs on the thread pool.
     */
    public String generateFullReportAsync() {
        String taskId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        taskStatus.put(taskId, "PENDING");

        // Trigger async processing
        buildReportInBackground(taskId);

        System.out.println("[ReportGenerator] Task " + taskId
                + " submitted to thread pool. Main thread continues.");
        return taskId;
    }

    /**
     * CO1 — @Async runs this on a separate worker thread from the pool.
     * The calling thread (HTTP request thread) is NOT blocked.
     *
     * Simulates a heavy report operation (aggregating JDBC data).
     * CompletableFuture<Void> is the return type for @Async void-equivalent.
     */
    @Async
    public CompletableFuture<Void> buildReportInBackground(String taskId) {
        String threadName = Thread.currentThread().getName();
        System.out.println("[ReportGenerator] Task " + taskId
                + " started on thread: " + threadName);

        taskStatus.put(taskId, "RUNNING");

        try {
            // Step 1: Gather top scorers via JDBC
            List<Map<String, Object>> topScorers = jdbcReportDao.getTopRunScorers();
            System.out.println("[ReportGenerator] Step 1/4 done — Top scorers: "
                    + topScorers.size() + " records. Thread: " + threadName);

            // Simulate processing time (in real app: PDF generation, file writing, etc.)
            Thread.sleep(500);

            // Step 2: Team stats
            List<Map<String, Object>> teamStats = jdbcReportDao.getTeamWinLossStats();
            System.out.println("[ReportGenerator] Step 2/4 done — Team stats. Thread: "
                    + threadName);

            Thread.sleep(300);

            // Step 3: Monthly match data
            List<Map<String, Object>> monthly = jdbcReportDao.getMatchesPerMonth();
            System.out.println("[ReportGenerator] Step 3/4 done — Monthly data. Thread: "
                    + threadName);

            Thread.sleep(200);

            // Step 4: Compile summary
            Map<String, Object> dashboard = jdbcReportDao.getDashboardStats();
            System.out.println("[ReportGenerator] Step 4/4 done — Dashboard stats. Thread: "
                    + threadName);

            // Mark complete with timestamp
            taskStatus.put(taskId, "COMPLETED at " + LocalDateTime.now()
                    + " | Thread: " + threadName
                    + " | Scorers: " + topScorers.size()
                    + " | Teams: " + teamStats.size()
                    + " | Months: " + monthly.size());

            System.out.println("[ReportGenerator] Task " + taskId
                    + " COMPLETED successfully on thread: " + threadName);

        } catch (InterruptedException e) {
            taskStatus.put(taskId, "INTERRUPTED");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            taskStatus.put(taskId, "FAILED: " + e.getMessage());
            System.err.println("[ReportGenerator] Task " + taskId + " FAILED: " + e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Check the status of an async report task.
     */
    public String getTaskStatus(String taskId) {
        return taskStatus.getOrDefault(taskId, "TASK_NOT_FOUND");
    }
}