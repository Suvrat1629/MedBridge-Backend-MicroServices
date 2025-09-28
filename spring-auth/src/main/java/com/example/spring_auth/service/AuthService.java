package com.example.spring_auth.service;

import com.example.spring_auth.dto.*;
import com.example.spring_auth.model.User;
import com.example.spring_auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate tokens
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("email", user.getEmail());
        extraClaims.put("fullName", user.getFullName());
        extraClaims.put("roles", user.getRoles());
        extraClaims.put("abhaNumber", user.getAbhaNumber());

        String accessToken = jwtService.generateAccessToken(user.getUsername(), extraClaims);
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getAbhaNumber(),
                user.getRoles()
        );

        log.info("Login successful for user: {}", request.getUsername());
        return new AuthResponse(accessToken, refreshToken, "Bearer", 3600L, userInfo);
    }

    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for user: {}", request.getUsername());

        // Check if user already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAbhaNumber(request.getAbhaNumber());
        user.setRoles(request.getRoles());
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());

        user = userRepository.save(user);

        // Generate tokens
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("email", user.getEmail());
        extraClaims.put("fullName", user.getFullName());
        extraClaims.put("roles", user.getRoles());
        extraClaims.put("abhaNumber", user.getAbhaNumber());

        String accessToken = jwtService.generateAccessToken(user.getUsername(), extraClaims);
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getAbhaNumber(),
                user.getRoles()
        );

        log.info("Registration successful for user: {}", request.getUsername());
        return new AuthResponse(accessToken, refreshToken, "Bearer", 3600L, userInfo);
    }

    public TokenValidationResponse validateToken(String token) {
        try {
            // Just validate the JWT token
            if (jwtService.validateToken(token)) {
                String username = jwtService.extractUsername(token);
                return TokenValidationResponse.valid(username, "", "", null);
            } else {
                return TokenValidationResponse.invalid("Invalid token");
            }
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return TokenValidationResponse.invalid("Token validation failed");
        }
    }


    public AuthResponse refreshToken(String refreshToken) {
        try {
            if (jwtService.validateToken(refreshToken)) {
                String username = jwtService.extractUsername(refreshToken);
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                // Generate new access token
                Map<String, Object> extraClaims = new HashMap<>();
                extraClaims.put("email", user.getEmail());
                extraClaims.put("fullName", user.getFullName());
                extraClaims.put("roles", user.getRoles());
                extraClaims.put("abhaNumber", user.getAbhaNumber());

                String newAccessToken = jwtService.generateAccessToken(user.getUsername(), extraClaims);
                String newRefreshToken = jwtService.generateRefreshToken(user.getUsername());

                AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getAbhaNumber(),
                        user.getRoles()
                );

                return new AuthResponse(newAccessToken, newRefreshToken, "Bearer", 3600L, userInfo);
            }
            throw new RuntimeException("Invalid refresh token");
        } catch (Exception e) {
            log.error("Token refresh error: {}", e.getMessage());
            throw new RuntimeException("Token refresh failed");
        }
    }
}
