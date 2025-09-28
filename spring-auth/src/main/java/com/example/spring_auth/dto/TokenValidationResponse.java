package com.example.spring_auth.dto;

import com.example.spring_auth.model.User;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;

@Data
public class TokenValidationResponse {
    private boolean valid;
    private String username;
    private String email;
    private String fullName;
    private Set<String> roles;
    private String message;

    // ADD THIS METHOD
    public static TokenValidationResponse valid(String username, String email, String fullName, Set<User.Role> userRoles) {
        TokenValidationResponse response = new TokenValidationResponse();
        response.valid = true;
        response.username = username;
        response.email = email;
        response.fullName = fullName;

        // Convert User.Role enum to String, handle null case
        if (userRoles != null) {
            response.roles = userRoles.stream()
                    .map(role -> role.name())
                    .collect(Collectors.toSet());
        } else {
            response.roles = Set.of();
        }

        response.message = "Token is valid";
        return response;
    }

    // Simple valid method for basic validation
    public static TokenValidationResponse valid(String username) {
        TokenValidationResponse response = new TokenValidationResponse();
        response.valid = true;
        response.username = username;
        response.email = "";
        response.fullName = "";
        response.roles = Set.of();
        response.message = "Token is valid";
        return response;
    }

    public static TokenValidationResponse invalid(String message) {
        TokenValidationResponse response = new TokenValidationResponse();
        response.valid = false;
        response.message = message;
        return response;
    }
}
