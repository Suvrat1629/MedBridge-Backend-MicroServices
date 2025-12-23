package com.example.spring_auth.service;

import com.example.spring_auth.dto.*;
import com.example.spring_auth.model.User;
import com.example.spring_auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthenticationManager authenticationManager;
    private AuthService authService;

    @BeforeEach
    public void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        authenticationManager = mock(AuthenticationManager.class);

        authService = new AuthService(userRepository, passwordEncoder, jwtService, authenticationManager);
    }

    @Test
    public void testLogin_success_returnsTokensAndUserInfo() {
        LoginRequest req = new LoginRequest();
        req.setUsername("alice");
        req.setPassword("secret");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(ArgumentMatchers.any())).thenReturn(auth);

        User user = new User();
        user.setId("u1");
        user.setUsername("alice");
        user.setEmail("a@ex.com");
        user.setFullName("Alice");
        user.setAbhaNumber("ABHA123");
        // User.setRoles expects a List<User.Role>
        user.setRoles(Set.of(User.Role.PATIENT));
        user.setLastLogin(LocalDateTime.now());

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(eq("alice"), anyMap())).thenReturn("ACCESS-TOK");
        when(jwtService.generateRefreshToken("alice")).thenReturn("REFRESH-TOK");

        AuthResponse resp = authService.login(req);

        assertNotNull(resp);
        assertEquals("ACCESS-TOK", resp.getAccessToken());
        assertEquals("REFRESH-TOK", resp.getRefreshToken());
        assertEquals("Bearer", resp.getTokenType());
        // AuthResponse field is 'user' (lombok generates getUser())
        assertNotNull(resp.getUser());
        assertEquals("alice", resp.getUser().getUsername());

        verify(userRepository).save(any(User.class)); // lastLogin updated and saved
    }

    @Test
    public void testRegister_usernameExists_throws() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("bob");
        req.setEmail("bob@ex.com");
        req.setPassword("pw");

        when(userRepository.existsByUsername("bob")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(req));
    }

    @Test
    public void testRefreshToken_success_returnsNewTokens() {
        String refreshToken = "REFRESH-TOK";
        when(jwtService.validateToken(refreshToken)).thenReturn(true);
        when(jwtService.extractUsername(refreshToken)).thenReturn("carol");

        User user = new User();
        user.setUsername("carol");
        user.setEmail("c@ex.com");
        user.setFullName("Carol");
        user.setAbhaNumber("ABHA-C");
        user.setRoles(Set.of(User.Role.PATIENT));

        when(userRepository.findByUsername("carol")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(eq("carol"), anyMap())).thenReturn("NEW-ACCESS");
        when(jwtService.generateRefreshToken("carol")).thenReturn("NEW-REFRESH");

        AuthResponse out = authService.refreshToken(refreshToken);
        assertNotNull(out);
        assertEquals("NEW-ACCESS", out.getAccessToken());
        assertEquals("NEW-REFRESH", out.getRefreshToken());
    }

    @Test
    public void testValidateToken_invalid_returnsInvalidResponse() {
        when(jwtService.validateToken("bad")).thenReturn(false);
        TokenValidationResponse resp = authService.validateToken("bad");
        assertNotNull(resp);
        assertFalse(resp.isValid());
    }
}