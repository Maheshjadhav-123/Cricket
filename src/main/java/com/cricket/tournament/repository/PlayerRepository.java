package com.cricket.tournament.repository;

import com.cricket.tournament.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * CO3 — Spring Data JPA Repository for Player
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByTeamId(Long teamId);
    List<Player> findByRoleIgnoreCase(String role);
}