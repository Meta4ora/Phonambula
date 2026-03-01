package com.example.server.controller;

import com.example.server.model.User;
import com.example.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        // Убираем пароли из ответа для безопасности
        users.forEach(user -> user.setPassword(null));
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> {
                    user.setPassword(null); // Не возвращаем пароль
                    return new ResponseEntity<>(user, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/create-simple")
    public ResponseEntity<User> createUserSimple(
            @RequestParam String surname,
            @RequestParam String name,
            @RequestParam(required = false) String patronymic,
            @RequestParam String login,
            @RequestParam String password,
            @RequestParam Integer roleId) {

        // Валидация обязательных полей
        if (surname == null || surname.trim().isEmpty() ||
                name == null || name.trim().isEmpty() ||
                login == null || login.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                roleId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            // Если patronymic не передан, используем пустую строку или null
            if (patronymic == null) {
                patronymic = "";
            }

            User createdUser = userService.createUser(
                    surname, name, patronymic, login, password, roleId
            );

            createdUser.setPassword(null); // Не возвращаем пароль
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Логируем ошибку и возвращаем BAD_REQUEST с сообщением
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody Map<String, Object> payload) {
        try {
            // Извлекаем все поля из JSON
            String surname = (String) payload.get("surname");
            String name = (String) payload.get("name");
            String patronymic = (String) payload.get("patronymic");
            String login = (String) payload.get("login");
            String password = (String) payload.get("password");

            // ИЗВЛЕКАЕМ ID ИЗ ВЛОЖЕННОГО ОБЪЕКТА idRole
            Map<String, Object> idRole = (Map<String, Object>) payload.get("idRole");
            Integer roleId = null;
            if (idRole != null) {
                roleId = (Integer) idRole.get("id");
            }

            // Валидация
            if (surname == null || surname.trim().isEmpty() ||
                    name == null || name.trim().isEmpty() ||
                    login == null || login.trim().isEmpty() ||
                    password == null || password.trim().isEmpty() ||
                    roleId == null) {

                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            if (patronymic == null) {
                patronymic = "";
            }

            // Создаем пользователя через сервис (пароль захешируется)
            User createdUser = userService.createUser(
                    surname, name, patronymic, login, password, roleId
            );

            createdUser.setPassword(null);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        if (!userService.findById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        try {
            User updatedUser = userService.update(id, user);
            updatedUser.setPassword(null); // Не возвращаем пароль
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userService.findById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        userService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/by-login/{login}")
    public ResponseEntity<User> getUserByLogin(@PathVariable String login) {
        return userService.findByLogin(login)
                .map(user -> {
                    user.setPassword(null);
                    return new ResponseEntity<>(user, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/check-login")
    public ResponseEntity<?> checkLogin(@RequestBody Map<String, String> credentials) {
        try {
            String login = credentials.get("login");
            String password = credentials.get("password");

            if (login == null || login.trim().isEmpty() ||
                    password == null || password.trim().isEmpty()) {
                return new ResponseEntity<>("Логин и пароль обязательны", HttpStatus.BAD_REQUEST);
            }

            boolean exists = userService.checkUserExists(login, password);

            if (exists) {
                return new ResponseEntity<>(Map.of("exists", true), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(Map.of("exists", false), HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}