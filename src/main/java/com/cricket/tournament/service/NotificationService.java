package com.cricket.tournament.service;

import com.cricket.tournament.entity.Notification;
import com.cricket.tournament.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Feature 7: In-App Notification System Service
 *
 * Auto-generates notifications on key events:
 *   - Match creation / start / completion
 *   - Score updates / results
 *   - Auction events
 *   - Fixture generation
 */
@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /** Create a new notification */
    public Notification createNotification(String message, String type, String icon) {
        Notification notif = new Notification(message, type, icon);
        return notificationRepository.save(notif);
    }

    /** Get all notifications (latest first, max 20) */
    @Transactional(readOnly = true)
    public List<Notification> getRecentNotifications() {
        return notificationRepository.findTop20ByOrderByCreatedAtDesc();
    }

    /** Get unread notifications */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications() {
        return notificationRepository.findByReadFalseOrderByCreatedAtDesc();
    }

    /** Get unread count */
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return notificationRepository.countByReadFalse();
    }

    /** Mark a notification as read */
    public void markAsRead(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + id));
        n.setRead(true);
        notificationRepository.save(n);
    }

    /** Mark all as read */
    public void markAllAsRead() {
        List<Notification> unread = notificationRepository.findByReadFalseOrderByCreatedAtDesc();
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    /** Get summary for header badge */
    @Transactional(readOnly = true)
    public Map<String, Object> getNotificationSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("unreadCount", getUnreadCount());
        summary.put("notifications", getRecentNotifications());
        return summary;
    }
}
