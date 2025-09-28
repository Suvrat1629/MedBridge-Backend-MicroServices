package com.example.spring_auth.controller;

import com.example.spring_auth.dto.TokenValidationResponse;
import com.example.spring_auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
@Slf4j
public class TokenController {

    private final JwtService jwtService;

    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        log.info("🔐 Token validation request received");
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("❌ Invalid authorization header format");
                return ResponseEntity.ok(TokenValidationResponse.invalid("Invalid authorization header format"));
            }

            String token = authHeader.replace("Bearer ", "").trim();
            log.info("🔍 Validating token: {}...", token.substring(0, Math.min(token.length(), 20)));

            boolean isValid = jwtService.validateToken(token);
            log.info("Token validation result: {}", isValid ? "VALID" : "INVALID");

            if (isValid) {
                String username = jwtService.extractUsername(token);
                log.info("✅ Token valid for username: {}", username);
                return ResponseEntity.ok(TokenValidationResponse.valid(username)); // Use simple method
            } else {
                log.warn("❌ Token validation failed");
                return ResponseEntity.ok(TokenValidationResponse.invalid("Invalid token"));
            }
        } catch (Exception e) {
            log.error("💥 Token validation exception: {}", e.getMessage(), e);
            return ResponseEntity.ok(TokenValidationResponse.invalid("Token validation failed: " + e.getMessage()));
        }
    }
}
