package com.cricket.tournament.repository;

import com.cricket.tournament.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * CO3 — Spring Data JPA Repository for User
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findByApprovedFalse();          // Pending approval queue
    List<User> findByRole(String role);
}