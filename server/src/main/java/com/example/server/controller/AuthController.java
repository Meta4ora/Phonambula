package com.example.server.controller;

import com.example.server.model.AuditLog;
import com.example.server.model.User;
import com.example.server.model.Subscriber;
import com.example.server.service.AuditLogService;
import com.example.server.service.JwtService;
import com.example.server.service.SubscriberService;
import com.example.server.service.UserService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final SubscriberService subscriberService;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;

    public AuthController(UserService userService,
                          SubscriberService subscriberService,
                          JwtService jwtService,
                          AuditLogService auditLogService) {
        this.userService = userService;
        this.subscriberService = subscriberService;
        this.jwtService = jwtService;
        this.auditLogService = auditLogService;
    }

    // LOGIN

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {

        User user = userService.authenticate(
                request.getLogin(),
                request.getPassword()
        ).orElseThrow(() ->
                new RuntimeException("Неверный логин или пароль")
        );

        String token = jwtService.generateToken(user.getLogin());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "login", user.getLogin(),
                "role", user.getIdRole().getNameRole()
        ));
    }

    // REGISTER

        @PostMapping("/register")
        @Transactional
        public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        
        System.out.println("Register request received: " + request);
        System.out.println("Mobile phone: " + request.getMobilePhoneNumber());
        System.out.println("Landline phone: " + request.getLandlinePhoneNumber());
        System.out.println("Internal phone: " + request.getInternalPhoneNumber());
        System.out.println("Cabinet: " + request.getCabinetNumber());

        try {
                // Создаем пользователя
                User user = userService.createUser(
                        request.getSurname() != null ? request.getSurname() : "",
                        request.getName() != null ? request.getName() : "",
                        request.getPatronymic() != null ? request.getPatronymic() : "",
                        request.getLogin(),
                        request.getPassword(),
                        request.getRoleId()
                );

                // Создаем абонента и получаем сохраненный объект
                Subscriber savedSubscriber = subscriberService.createSubscriber(
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

                // Получаем текущего пользователя для аудита (кто выполняет регистрацию)
                User currentUser = null;
                try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getName() != null && !auth.getName().equals("anonymousUser")) {
                        currentUser = userService.findByLogin(auth.getName()).orElse(null);
                }
                } catch (Exception e) {
                System.out.println("Не удалось получить текущего пользователя для аудита: " + e.getMessage());
                }
                
                // Если не удалось получить текущего пользователя, используем созданного пользователя
                if (currentUser == null) {
                currentUser = user;
                }

                // Записываем в аудит создание пользователя
                Map<String, Object> userAfterMap = new HashMap<>();
                userAfterMap.put("id", user.getId());
                userAfterMap.put("login", user.getLogin());
                userAfterMap.put("surname", user.getSurname());
                userAfterMap.put("name", user.getName());
                userAfterMap.put("patronymic", user.getPatronymic());
                userAfterMap.put("role", user.getIdRole() != null ? user.getIdRole().getNameRole() : null);
                
                auditLogService.createAuditLog(
                currentUser,
                "INSERT",
                "users",
                user.getId().intValue(),
                null,
                userAfterMap
                );
                
                // Записываем в аудит создание абонента
                Map<String, Object> subscriberAfterMap = new HashMap<>();
                subscriberAfterMap.put("id", savedSubscriber.getId());
                subscriberAfterMap.put("userId", user.getId());
                subscriberAfterMap.put("fullName", String.format("%s %s %s", 
                user.getSurname(), 
                user.getName(), 
                user.getPatronymic() != null ? user.getPatronymic() : "").trim());
                subscriberAfterMap.put("mobilePhone", savedSubscriber.getMobilePhoneNumber());
                subscriberAfterMap.put("landlinePhone", savedSubscriber.getLandlinePhoneNumber());
                subscriberAfterMap.put("internalPhone", savedSubscriber.getInternalPhoneNumber());
                subscriberAfterMap.put("cabinet", savedSubscriber.getCabinetNumber());
                subscriberAfterMap.put("position", savedSubscriber.getIdPost() != null ? savedSubscriber.getIdPost().getNamePost() : null);
                subscriberAfterMap.put("department", savedSubscriber.getIdDivision() != null ? savedSubscriber.getIdDivision().getNameDivision() : null);
                subscriberAfterMap.put("building", savedSubscriber.getIdBuilding() != null ? savedSubscriber.getIdBuilding().getNameBuilding() : null);
                
                auditLogService.createAuditLog(
                currentUser,
                "INSERT",
                "subscribers",
                savedSubscriber.getId().intValue(),
                null,
                subscriberAfterMap
                );

                String token = jwtService.generateToken(user.getLogin());

                return ResponseEntity.ok(Map.of(
                        "token", token,
                        "login", user.getLogin(),
                        "role", user.getIdRole().getNameRole()
                ));
                
        } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
        }

    // DTO 

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
        
        private String mobilePhoneNumber;
        private String landlinePhoneNumber;
        private String internalPhoneNumber;
        private String cabinetNumber;
    }
}