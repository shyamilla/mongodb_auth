package com.talentsprint.mongodb.controllers;

import com.talentsprint.mongodb.model.User;
import com.talentsprint.mongodb.repository.UserRepository;
import com.talentsprint.mongodb.config.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Random;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // REGISTER (username, email, password)
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent())
            return "Username already exists";
        if (userRepository.findByEmail(user.getEmail()).isPresent())
            return "Email already exists";

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "Registered successfully";
    }

    // LOGIN (email, password)
    @PostMapping("/login")
    public String login(@RequestBody User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isEmpty())
            return "Invalid email";

        if (!passwordEncoder.matches(user.getPassword(), existingUser.get().getPassword()))
            return "Invalid password";

        return "Login Successful!!!";
        // return jwtUtil.generateToken(existingUser.get().getUsername()); // token
        // still uses username internally
    }

    // WELCOME (JWT protected)
    @GetMapping("/welcome")
    public String welcome(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return "Missing or invalid token";
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        if (!jwtUtil.validateToken(token, username))
            return "Invalid token";
        return "Welcome, " + username + "!";
    }

    // FORGOT PASSWORD (only email)


@PostMapping("/forgot")
public String forgotPassword(@RequestBody Map<String, String> body) {
    String email = body.get("email");
    Optional<User> user = userRepository.findByEmail(email);

    if (user.isEmpty()) return "Email not found";

    String otp = String.format("%06d", new Random().nextInt(999999));
    user.get().setResetOtp(otp);
    userRepository.save(user.get());

    // send OTP email
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(email);
    message.setSubject("Password Reset OTP");
    message.setText("Your OTP is: " + otp);
    mailSender.send(message);

    return "OTP sent successfully to email: " + email;
}

@Autowired
private JavaMailSender mailSender;


    @PostMapping("/reset")
    public String resetPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");
        String newPassword = body.get("newPassword");

        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty())
            return "Email not found";

        if (!otp.equals(user.get().getResetOtp()))
            return "Invalid OTP";

        user.get().setPassword(passwordEncoder.encode(newPassword));
        user.get().setResetOtp(null);
        userRepository.save(user.get());

        return "Password reset successfully";
    }

    @PostMapping("/logout")
    public String logout() {
        return "Logged out successfully";
    }
}
