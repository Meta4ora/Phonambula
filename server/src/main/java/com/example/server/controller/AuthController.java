package com.example.server.controller;

import com.example.server.model.User;
import com.example.server.service.JwtService;
import com.example.server.service.SubscriberService;
import com.example.server.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final SubscriberService subscriberService;
    private final JwtService jwtService;

    public AuthController(UserService userService,
                          SubscriberService subscriberService,
                          JwtService jwtService) {
        this.userService = userService;
        this.subscriberService = subscriberService;
        this.jwtService = jwtService;
    }

    // ================= LOGIN =================

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            // Валидация входных данных из второго файла
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

            // Используем generateToken с ролью из второго файла
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

    // ================= REGISTER =================

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Расширенная валидация из второго файла
            if (request.getSurname() == null || request.getSurname().trim().isEmpty() ||
                    request.getName() == null || request.getName().trim().isEmpty() ||
                    request.getLogin() == null || request.getLogin().trim().isEmpty() ||
                    request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return new ResponseEntity<>("Все обязательные поля должны быть заполнены", HttpStatus.BAD_REQUEST);
            }

            System.out.println("Register request received: " + request);
            System.out.println("Mobile phone: " + request.getMobilePhoneNumber());
            System.out.println("Landline phone: " + request.getLandlinePhoneNumber());
            System.out.println("Internal phone: " + request.getInternalPhoneNumber());
            System.out.println("Cabinet: " + request.getCabinetNumber());

            // Используем роль из запроса или значение по умолчанию 2
            Integer roleId = request.getRoleId() != null ? request.getRoleId() : 2;

            // 1️⃣ создаём пользователя
            User user = userService.createUser(
                    request.getSurname() != null ? request.getSurname() : "",
                    request.getName() != null ? request.getName() : "",
                    request.getPatronymic() != null ? request.getPatronymic() : "",
                    request.getLogin(),
                    request.getPassword(),
                    roleId,
                    null  // сервис сам подставит системного пользователя
            );

            // 2️⃣ создаём subscriber со всеми полями
            subscriberService.createSubscriber(
                    user.getId(),
                    request.getPostId(),
                    request.getDivisionId(),
                    request.getBuildingId(),
                    null, // dateBirth
                    request.getCabinetNumber() != null ? request.getCabinetNumber() : "",
                    request.getInternalPhoneNumber() != null ? request.getInternalPhoneNumber() : "",
                    request.getLandlinePhoneNumber() != null ? request.getLandlinePhoneNumber() : "",
                    request.getMobilePhoneNumber() != null ? request.getMobilePhoneNumber() : ""
            );

            // 3️⃣ выдаём JWT сразу после регистрации с ролью
            String token = jwtService.generateToken(
                    user.getLogin(),
                    user.getIdRole().getNameRole()
            );

            user.setPassword(null);

            return new ResponseEntity<>(Map.of(
                    "token", token,
                    "user", user,
                    "login", user.getLogin(),
                    "role", user.getIdRole().getNameRole()
            ), HttpStatus.CREATED);

        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Ошибка при регистрации", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================= VALIDATE TOKEN =================

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

    // ================= DTO =================

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

        private Integer roleId;
        private Integer postId;
        private Integer divisionId;
        private Integer buildingId;

        // Поля для телефонов и кабинета из первого файла
        private String mobilePhoneNumber;
        private String landlinePhoneNumber;
        private String internalPhoneNumber;
        private String cabinetNumber;
    }
}