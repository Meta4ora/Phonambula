package com.example.server;

import com.example.server.service.JwtService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    // успешная генерация и извлечение данных из токена
    @Test
    void generateAndExtractToken_success() {
        String login = "admin";
        String role = "ADMIN";

        String token = jwtService.generateToken(login, role);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);

        String extractedLogin = jwtService.extractLogin(token);
        assertEquals(login, extractedLogin);
    }

    // генерация разных токенов для разных пользователей
    @Test
    void generateToken_withDifferentUsers_returnsDifferentTokens() {
        String token1 = jwtService.generateToken("admin", "ADMIN");
        String token2 = jwtService.generateToken("user", "USER");

        assertNotEquals(token1, token2);
    }

    // извлечение логина из токена
    @Test
    void extractLogin_returnsCorrectLogin() {
        String login = "ivanov";
        String token = jwtService.generateToken(login, "USER");

        String extracted = jwtService.extractLogin(token);

        assertEquals(login, extracted);
    }

    // многократное извлечение возвращает один и тот же логин
    @Test
    void multipleExtractions_returnSameLogin() {
        String login = "petrov";
        String token = jwtService.generateToken(login, "ADMIN");

        String extracted1 = jwtService.extractLogin(token);
        String extracted2 = jwtService.extractLogin(token);

        assertEquals(login, extracted1);
        assertEquals(login, extracted2);
        assertEquals(extracted1, extracted2);
    }
}