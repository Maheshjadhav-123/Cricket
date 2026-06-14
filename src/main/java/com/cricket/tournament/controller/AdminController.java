package com.cricket.tournament.controller;

import com.cricket.tournament.entity.User;
import com.cricket.tournament.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Admin-only controller for user management.
 * Role guard: every method checks session role = ADMIN.
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // ── Helper: enforce ADMIN role ─────────────────────────────────
    private boolean isAdmin(HttpSession session) {
        Object role = session.getAttribute("userRole");
        return "ADMIN".equalsIgnoreCase(role != null ? role.toString() : "");
    }

    /** GET /api/admin/users — All users */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Admin access required."));
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /** GET /api/admin/users/pending — Users awaiting approval */
    @GetMapping("/users/pending")
    public ResponseEntity<?> getPendingUsers(HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Admin access required."));
        List<User> pending = userService.getPendingUsers();
        return ResponseEntity.ok(pending);
    }

    /** POST /api/admin/users/{id}/approve — Approve a user */
    @PostMapping("/users/{id}/approve")
    public ResponseEntity<?> approveUser(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Admin access required."));
        try {
            User approved = userService.approveUser(id);
            return ResponseEntity.ok(Map.of(
                "message",  "User '" + approved.getUsername() + "' approved successfully.",
                "username", approved.getUsername()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /** DELETE /api/admin/users/{id} — Delete a user */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Admin access required."));
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted."));
    }
}