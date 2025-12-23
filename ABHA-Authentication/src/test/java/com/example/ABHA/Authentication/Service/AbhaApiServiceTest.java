package com.example.ABHA.Authentication.Service;

import com.example.ABHA.Authentication.Config.AbhaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class AbhaApiServiceTest {

    @Test
    public void testConstructor_buildsWebClientField() throws Exception {
        // minimal config stub
        AbhaConfig cfg = new AbhaConfig();
        cfg.setBaseUrl("http://localhost");
        cfg.setAuthToken("token");
        cfg.setXHipId("hip");
        cfg.setConnectionTimeoutSeconds(1);
        cfg.setReadTimeoutSeconds(1);

        ObjectMapper mapper = new ObjectMapper();
        // pass null for EncryptionService since this test only verifies webClient construction
        AbhaApiService svc = new AbhaApiService(cfg, mapper, null);

        // verify private webClient field present (non-null)
        Field webClientField = AbhaApiService.class.getDeclaredField("webClient");
        webClientField.setAccessible(true);
        Object webClient = webClientField.get(svc);
        assertNotNull(webClient, "webClient should be constructed by constructor");
    }
}