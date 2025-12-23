package com.example.ABHA.Authentication.Controller;

import com.example.ABHA.Authentication.Model.AbhaProfile;
import com.example.ABHA.Authentication.Response.AbhaResponse;
import com.example.ABHA.Authentication.Service.AbhaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AbhaControllerTest {

    private AbhaController controller;

    @BeforeEach
    public void setup() {
        // fake service that returns simple success AbhaResponse objects
        AbhaService fakeService = new AbhaService(null, null, null, null) {
            @Override
            public Mono<String> initialize() {
                return Mono.just("OK");
            }

            @Override
            public Mono<AbhaResponse<Boolean>> checkHealthIdExists(String healthId) {
                return Mono.just(AbhaResponse.success(true));
            }

            @Override
            public Mono<AbhaResponse<String>> startRegistrationWithAadhaar(String aadhaar) {
                return Mono.just(AbhaResponse.success("T1", "T1"));
            }

            @Override
            public Mono<AbhaResponse<String>> generateMobileOtpForRegistration(String txnId, String mobile) {
                return Mono.just(AbhaResponse.success("MOB-TXN", "MOB-TXN"));
            }

            @Override
            public Mono<AbhaResponse<AbhaProfile>> getProfile(String authToken) {
                AbhaProfile p = new AbhaProfile();
                p.setName("Test");
                return Mono.just(AbhaResponse.success(p));
            }

            @Override
            public Mono<AbhaResponse<byte[]>> getQrCode(String authToken) {
                return Mono.just(AbhaResponse.success(new byte[]{1,2,3}));
            }
        };

        controller = new AbhaController(fakeService);
    }

    @Test
    public void testInitialize_returnsOkResponseEntity() {
        ResponseEntity<AbhaResponse<String>> resp = controller.initialize().block();
        assertNotNull(resp);
        assertEquals(200, resp.getStatusCodeValue());
        AbhaResponse<?> body = resp.getBody();
        assertNotNull(body);
    }

    @Test
    public void testCheckHealthIdExists_and_GetProfile_and_QrCode() throws Exception {
        ResponseEntity<AbhaResponse<Boolean>> r1 = controller.checkHealthIdExists("h1").block();
        assertEquals(200, r1.getStatusCodeValue());
        AbhaResponse<?> body1 = r1.getBody();
        Method getData = body1.getClass().getMethod("getData");
        Object d = getData.invoke(body1);
        assertEquals(Boolean.TRUE, d);

        ResponseEntity<AbhaResponse<AbhaProfile>> r2 = controller.getProfile("tok").block();
        assertEquals(200, r2.getStatusCodeValue());
        assertNotNull(r2.getBody().getData());

        ResponseEntity<AbhaResponse<byte[]>> r3 = controller.getQrCode("tok").block();
        assertEquals(200, r3.getStatusCodeValue());
        assertArrayEquals(new byte[]{1,2,3}, r3.getBody().getData());
    }
}