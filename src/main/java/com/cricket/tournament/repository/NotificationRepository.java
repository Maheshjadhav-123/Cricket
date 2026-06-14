package com.cricket.tournament.repository;

import com.cricket.tournament.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Feature 7 — JPA Repository for Notification entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReadFalseOrderByCreatedAtDesc();
    List<Notification> findTop20ByOrderByCreatedAtDesc();
    long countByReadFalse();
}
