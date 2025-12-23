package com.example.spring_auth.controller;

import com.example.spring_auth.dto.*;
import com.example.spring_auth.model.User;
import com.example.spring_auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    private AuthService authService;
    private AuthController controller;

    @BeforeEach
    public void setup() {
        authService = mock(AuthService.class);
        controller = new AuthController(authService);
    }

    @Test
    public void testLogin_ok_onSuccess() {
        LoginRequest req = new LoginRequest();
        req.setUsername("alice");
        req.setPassword("pw");

        AuthResponse ar = new AuthResponse("A", "R", "Bearer", 3600L,
                new AuthResponse.UserInfo("id","alice","a@e","Alice","ABHA", Set.of(User.Role.PATIENT)));
        when(authService.login(req)).thenReturn(ar);

        ResponseEntity<AuthResponse> resp = controller.login(req);
        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertEquals("A", resp.getBody().getAccessToken());
    }

    @Test
    public void testLogin_badRequest_onException() {
        LoginRequest req = new LoginRequest();
        req.setUsername("bad");
        req.setPassword("pw");

        when(authService.login(req)).thenThrow(new RuntimeException("fail"));

        ResponseEntity<AuthResponse> resp = controller.login(req);
        assertEquals(400, resp.getStatusCodeValue());
    }

    @Test
    public void testValidateToken_headerMalformed_returnsInvalid() {
        ResponseEntity<TokenValidationResponse> resp = controller.validateToken("NotBearer token");
        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertFalse(resp.getBody().isValid());
    }

    @Test
    public void testValidateToken_delegatesToService() {
        TokenValidationResponse ok = TokenValidationResponse.valid("alice", "", "", null);
        when(authService.validateToken("TOK")).thenReturn(ok);

        ResponseEntity<TokenValidationResponse> resp = controller.validateToken("Bearer TOK");
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody().isValid());
        verify(authService).validateToken("TOK");
    }

    @Test
    public void testRefreshToken_ok_and_health() {
        Map<String,String> req = Map.of("refreshToken", "RTOK");
        AuthResponse ar = new AuthResponse("A", "R", "Bearer", 3600L,
                new AuthResponse.UserInfo("id","u","e","U","ABHA", Set.of(User.Role.PATIENT)));
        when(authService.refreshToken("RTOK")).thenReturn(ar);

        ResponseEntity<AuthResponse> resp = controller.refreshToken(req);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("A", resp.getBody().getAccessToken());

        ResponseEntity<java.util.Map<String, Object>> health = controller.health();
        assertEquals(200, health.getStatusCodeValue());
        assertEquals("UP", health.getBody().get("status"));
    }
}