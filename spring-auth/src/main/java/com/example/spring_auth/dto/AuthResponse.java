package com.example.spring_auth.dto;

import com.example.spring_auth.model.User;
import lombok.Data;
import lombok.AllArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserInfo user;
    
    @Data
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String abhaNumber;
        private Set<User.Role> roles;
    }
}
