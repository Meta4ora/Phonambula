package com.example.server.controller;

import com.example.server.model.User;
import com.example.server.service.JwtService;
import com.example.server.service.UserService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService,
                          JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {

        User user = userService.authenticate(
                request.getLogin(),
                request.getPassword()
        ).orElseThrow(() ->
                new RuntimeException("Неверный логин или пароль")
        );

        String token = jwtService.generateToken(
                user.getLogin(),
                user.getIdRole().getNameRole()
        );

        return ResponseEntity.ok(Map.of(
                "token", token,
                "login", user.getLogin(),
                "role", user.getIdRole().getNameRole()
        ));
    }

    @Data
    static class AuthRequest {
        private String login;
        private String password;
    }
}