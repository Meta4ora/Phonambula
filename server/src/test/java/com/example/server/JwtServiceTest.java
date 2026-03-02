package com.example.server;

import com.example.server.service.JwtService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    @Test
    void generateAndExtractToken_success() {
        String token = jwtService.generateToken("admin", "ADMIN");

        assertNotNull(token);

        String login = jwtService.extractLogin(token);

        assertEquals("admin", login);
    }
}