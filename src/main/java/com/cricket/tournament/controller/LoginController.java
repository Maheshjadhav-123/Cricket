package com.cricket.tournament.controller;

import com.cricket.tournament.entity.User;
import com.cricket.tournament.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * CO2 — Session-based Login and Logout controller.
 *
 * On successful login:
 *   - Stores user object in HttpSession
 *   - Stores role string for AuthFilter to check
 *
 * On logout:
 *   - Invalidates the session completely
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class LoginController {

    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    /**
     * POST /api/auth/login
     * Body: { "username": "...", "password": "..." }
     *
     * CO2 — HttpSession is injected by Spring MVC automatically.
     * We store the logged-in user into the session here.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body,
                                   HttpSession session) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Username and password are required."));
        }

        User user = userService.login(username, password);

        if (user == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid credentials or account not approved yet."));
        }

        // CO2 — Store user in HTTP Session
        session.setAttribute("loggedUser", user.getUsername());
        session.setAttribute("userRole",   user.getRole());
        session.setAttribute("userId",     user.getId());
        session.setMaxInactiveInterval(1800);  // 30 min timeout

        return ResponseEntity.ok(Map.of(
            "message",  "Login successful",
            "role",     user.getRole(),
            "username", user.getUsername(),
            "redirect", user.getRole().equalsIgnoreCase("ADMIN")
                        ? "/admin.html"
                        : user.getRole().equalsIgnoreCase("TEAM_MANAGER")
                        ? "/team-manager.html"
                        : "/user-dashboard.html"
        ));
    }

    /**
     * POST /api/auth/logout
     * CO2 — Invalidates the HTTP session completely.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }

    /**
     * GET /api/auth/me
     * Returns current session info (used by frontend to check login state).
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Object user = session.getAttribute("loggedUser");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in."));
        }
        return ResponseEntity.ok(Map.of(
            "username", user,
            "role",     session.getAttribute("userRole")
        ));
    }

    /**
     * POST /api/auth/register
     * Registers a new USER account (pending admin approval).
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            User created = userService.registerUser(
                body.get("username"),
                body.get("password"),
                body.get("email")
            );
            return ResponseEntity.ok(Map.of(
                "message", "Registration successful! Awaiting admin approval.",
                "username", created.getUsername()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}