package com.example.spring_auth.controller;

import com.example.spring_auth.dto.*;
import com.example.spring_auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getUsername(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Registration failed for user: {}", request.getUsername(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    // THIS IS THE MISSING ENDPOINT - ADD THIS
    @PostMapping("/validate-token")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        log.info("Token validation request received");
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Invalid authorization header format");
                return ResponseEntity.ok(TokenValidationResponse.invalid("Invalid authorization header format"));
            }

            String token = authHeader.replace("Bearer ", "").trim();
            log.info("Validating token: {}...", token.substring(0, Math.min(token.length(), 20)));

            TokenValidationResponse response = authService.validateToken(token);
            log.info("Token validation result: {}", response.isValid() ? "VALID" : "INVALID");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token validation error", e);
            return ResponseEntity.ok(TokenValidationResponse.invalid("Token validation failed: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            AuthResponse response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "spring-auth",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
