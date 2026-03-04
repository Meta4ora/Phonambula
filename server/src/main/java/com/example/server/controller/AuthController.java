package com.example.server.controller;

import com.example.server.model.User;
import com.example.server.service.JwtService;
import com.example.server.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

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
        try {
            // Валидация входных данных
            if (request.getLogin() == null || request.getLogin().trim().isEmpty() ||
                    request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return new ResponseEntity<>("Логин и пароль обязательны", HttpStatus.BAD_REQUEST);
            }

            Optional<User> userOpt = userService.authenticate(
                    request.getLogin(),
                    request.getPassword()
            );

            if (userOpt.isEmpty()) {
                return new ResponseEntity<>("Неверный логин или пароль", HttpStatus.UNAUTHORIZED);
            }

            User user = userOpt.get();
            String token = jwtService.generateToken(
                    user.getLogin(),
                    user.getIdRole().getNameRole()
            );

            // Не возвращаем пароль
            user.setPassword(null);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "user", user,
                    "login", user.getLogin(),
                    "role", user.getIdRole().getNameRole()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка при авторизации", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Валидация
            if (request.getSurname() == null || request.getSurname().trim().isEmpty() ||
                    request.getName() == null || request.getName().trim().isEmpty() ||
                    request.getLogin() == null || request.getLogin().trim().isEmpty() ||
                    request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return new ResponseEntity<>("Все обязательные поля должны быть заполнены", HttpStatus.BAD_REQUEST);
            }

            String patronymic = request.getPatronymic() != null ? request.getPatronymic() : "";
            Integer roleId = 2;

            // При регистрации передаем null - сервис сам подставит системного пользователя
            User createdUser = userService.createUser(
                    request.getSurname(),
                    request.getName(),
                    patronymic,
                    request.getLogin(),
                    request.getPassword(),
                    roleId,
                    null  // сервис сам подставит системного пользователя
            );

            // Генерируем токен для нового пользователя
            String token = jwtService.generateToken(
                    createdUser.getLogin(),
                    createdUser.getIdRole().getNameRole()
            );

            createdUser.setPassword(null);

            return new ResponseEntity<>(Map.of(
                    "token", token,
                    "user", createdUser
            ), HttpStatus.CREATED);

        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");

            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of("valid", false, "error", "Токен не предоставлен"));
            }

            String login = jwtService.extractLogin(token);

            if (login != null && !jwtService.isTokenExpired(token)) {
                Optional<User> userOpt = userService.findByLogin(login);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setPassword(null);
                    return ResponseEntity.ok(Map.of(
                            "valid", true,
                            "user", user
                    ));
                }
            }

            return ResponseEntity.ok(Map.of("valid", false));

        } catch (ExpiredJwtException e) {
            return ResponseEntity.ok(Map.of("valid", false, "error", "Токен истек"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("valid", false, "error", "Недействительный токен"));
        }
    }

    @Data
    static class AuthRequest {
        private String login;
        private String password;
    }

    @Data
    static class RegisterRequest {
        private String surname;
        private String name;
        private String patronymic;
        private String login;
        private String password;
    }
}