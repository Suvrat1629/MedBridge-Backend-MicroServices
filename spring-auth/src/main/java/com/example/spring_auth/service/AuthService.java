package com.example.spring_auth.service;

import com.example.spring_auth.dto.*;
import com.example.spring_auth.model.User;
import com.example.spring_auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        log.info("Login successful for user: {}", request.getUsername());
        return buildAuthResponse(user);
    }

    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRoles(request.getRoles());
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        log.info("Registration successful for user: {}", request.getUsername());
        return buildAuthResponse(user);
    }

    public TokenValidationResponse validateToken(String token) {
        try {
            if (jwtService.validateToken(token)) {
                String username = jwtService.extractUsername(token);
                return TokenValidationResponse.valid(username, "", "", null);
            }
            return TokenValidationResponse.invalid("Invalid token");
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return TokenValidationResponse.invalid("Token validation failed");
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        try {
            if (!jwtService.validateToken(refreshToken)) {
                throw new RuntimeException("Invalid refresh token");
            }
            String username = jwtService.extractUsername(refreshToken);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return buildAuthResponse(user);
        } catch (Exception e) {
            log.error("Token refresh error: {}", e.getMessage());
            throw new RuntimeException("Token refresh failed");
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        Map<String, Object> extraClaims = Map.of(
                "email", user.getEmail(),
                "fullName", user.getFullName(),
                "roles", user.getRoles()
        );

        String accessToken = jwtService.generateAccessToken(user.getUsername(), extraClaims);
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRoles()
        );

        return new AuthResponse(accessToken, refreshToken, "Bearer", 3600L, userInfo);
    }
}
