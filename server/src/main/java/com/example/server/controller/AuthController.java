package com.example.server.controller;

import com.example.server.model.User;
import com.example.server.service.JwtService;
import com.example.server.service.SubscriberService;
import com.example.server.service.UserService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

        User user = userService.createUser(
                request.getSurname() != null ? request.getSurname() : "",
                request.getName() != null ? request.getName() : "",
                request.getPatronymic() != null ? request.getPatronymic() : "",
                request.getLogin(),
                request.getPassword(),
                request.getRoleId()
        );

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

        String token = jwtService.generateToken(user.getLogin());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "login", user.getLogin(),
                "role", user.getIdRole().getNameRole()
        ));
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