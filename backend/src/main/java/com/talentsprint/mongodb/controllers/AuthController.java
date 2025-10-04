package com.talentsprint.mongodb.controllers;

import com.talentsprint.mongodb.config.JwtUtil;
import com.talentsprint.mongodb.model.User;
import com.talentsprint.mongodb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JavaMailSender mailSender;

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) return "Username exists";
        if (userRepository.findByEmail(user.getEmail()).isPresent()) return "Email exists";

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "Registered successfully";
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        Optional<User> existingUser  = userRepository.findByEmail(email);
        if (existingUser .isEmpty()) throw new RuntimeException("Invalid email/password");

        if (!passwordEncoder.matches(password, existingUser .get().getPassword()))
            throw new RuntimeException("Invalid email/password");

        // FIX: The principal for AuthenticationManager must match the principal used 
        // in UserDetailsService (which will be the username/email used for lookup).
        // Since we are finding by email, we use the email as the principal here.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(existingUser.get().getUsername(), password)
        );

        // FIX: Generate JWT token using the consistent principal (existingUser.get().getUsername() 
        // as the UserDetailsService in SecurityConfig is set to load by username). 
        // Ensure that existingUser.get().getUsername() is NOT null/empty.
        String token = jwtUtil.generateToken(existingUser.get().getUsername());
        return Map.of("token", token);
    }

    // This endpoint requires JWT token in Authorization header: "Bearer <token>"
    @GetMapping("/welcome")
    public String welcome(Authentication authentication) {
        return "Welcome, " + authentication.getName() + "!";
    }

    @PostMapping("/forgot")
    public String forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty())
            return "Email not found";

        String otp = String.format("%06d", new Random().nextInt(999999));
        user.get().setResetOtp(otp);
        userRepository.save(user.get());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset OTP");
        message.setText("Your OTP is: " + otp);
        mailSender.send(message);

        return "OTP sent successfully to email: " + email;
    }

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
        // Since JWT is stateless, logout can be handled client-side by deleting token
        return "Logged out successfully";
    }
}