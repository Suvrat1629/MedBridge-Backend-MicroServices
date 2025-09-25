package com.example.ABHA.Authentication.Controller;

import com.example.ABHA.Authentication.Response.AbhaResponse;
import com.example.ABHA.Authentication.Service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
@Slf4j
public class InternalAuthController {

    private final JwtService jwtService;

    @PostMapping("/validate-token")
    public ResponseEntity<AbhaResponse<Boolean>> validateToken(@RequestBody String token) {
        try {
            boolean isValid = jwtService.validateToken(token);
            return ResponseEntity.ok(AbhaResponse.success(isValid));
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return ResponseEntity.ok(AbhaResponse.success(false));
        }
    }

    @PostMapping("/extract-user")
    public ResponseEntity<AbhaResponse<String>> extractUserFromToken(@RequestBody String token) {
        try {
            String healthId = jwtService.extractHealthId(token);
            return ResponseEntity.ok(AbhaResponse.success(healthId));
        } catch (Exception e) {
            log.error("User extraction failed", e);
            return ResponseEntity.ok(AbhaResponse.error("Invalid token", "INVALID_TOKEN"));
        }
    }
}
