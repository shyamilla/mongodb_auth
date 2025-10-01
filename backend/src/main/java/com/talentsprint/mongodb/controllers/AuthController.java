package com.talentsprint.mongodb.controllers;

import com.talentsprint.mongodb.model.User;
import com.talentsprint.mongodb.repository.UserRepository;
import com.talentsprint.mongodb.config.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Random;

@RestController
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) return "Username already exists";
        if (userRepository.findByEmail(user.getEmail()).isPresent()) return "Email already exists";

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "Registered successfully";
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isEmpty()) return "Invalid username";

        if (!passwordEncoder.matches(user.getPassword(), existingUser.get().getPassword())) return "Invalid password";

        return jwtUtil.generateToken(user.getUsername());
    }

    @GetMapping("/welcome")
    public String welcome(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return "Missing or invalid token";
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        if (!jwtUtil.validateToken(token, username)) return "Invalid token";
        return "Welcome, " + username + "!";
    }

    @PostMapping("/forgot")
    public String forgotPassword(@RequestParam String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) return "Email not found";

        String otp = String.format("%06d", new Random().nextInt(999999));
        user.get().setResetOtp(otp);
        userRepository.save(user.get());

        // send OTP via email here if needed
        return "OTP sent to email: " + otp;
    }

    @PostMapping("/reset")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String otp,
                                @RequestParam String newPassword) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) return "Email not found";

        if (!otp.equals(user.get().getResetOtp())) return "Invalid OTP";

        user.get().setPassword(passwordEncoder.encode(newPassword));
        user.get().setResetOtp(null);
        userRepository.save(user.get());
        return "Password reset successfully";
    }

    @PostMapping("/logout")
    public String logout() {
        // JWT is stateless, client just deletes token
        return "Logged out successfully";
    }
}
