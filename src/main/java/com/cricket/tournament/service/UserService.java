package com.cricket.tournament.service;

import com.cricket.tournament.entity.User;
import com.cricket.tournament.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * CO1 (OOP) + CO3 (Spring) — Business logic for User management.
 * Handles registration, login, approval, and role management.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // BCrypt from SecurityConfig

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * CO2 — Session-based login.
     * Validates username exists, password matches BCrypt hash, and user is approved.
     * Returns User object on success, null on any failure.
     */
    @Transactional(readOnly = true)
    public User login(String username, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) return null;

        User user = userOpt.get();

        // BCrypt check: compare raw password against stored hash
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) return null;

        // ADMIN accounts auto-approved; USER accounts need admin approval
        if (!user.isApproved()) return null;

        return user;
    }

    /**
     * Self-registration for new users (role = USER by default).
     * Password is BCrypt-hashed before saving.
     * New USER registrations are pending approval (approved = false).
     */
    public User registerUser(String username, String rawPassword, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));  // BCrypt hash
        user.setEmail(email);
        user.setRole("USER");
        user.setApproved(false);  // Needs admin approval

        return userRepository.save(user);
    }

    /**
     * Admin creates a new ADMIN account (auto-approved).
     */
    public User createAdmin(String username, String rawPassword, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username '" + username + "' already exists.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEmail(email);
        user.setRole("ADMIN");
        user.setApproved(true);  // Admin auto-approved

        return userRepository.save(user);
    }

    /**
     * Admin approves a pending user registration.
     */
    public User approveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setApproved(true);
        return userRepository.save(user);
    }

    /**
     * Get all users waiting for admin approval.
     */
    @Transactional(readOnly = true)
    public List<User> getPendingUsers() {
        return userRepository.findByApprovedFalse();
    }

    /**
     * Get all users in the system.
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}