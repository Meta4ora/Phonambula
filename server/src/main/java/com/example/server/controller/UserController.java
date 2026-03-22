package com.example.server.controller;

import com.example.server.model.User;
import com.example.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

    // ================= МЕТОД ДЛЯ ПОЛУЧЕНИЯ ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ (из второго файла) =================

    /**
     * Метод для получения текущего пользователя из контекста безопасности
     */
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Проверяем, что principal это String (логин) и это не анонимный пользователь
        if (principal instanceof String) {
            String login = (String) principal;
            if (!"anonymousUser".equals(login)) {
                return userService.findByLogin(login)
                        .orElse(null); // Возвращаем null вместо исключения
            }
        }

        return null; // Возвращаем null для неаутентифицированных запросов
    }

    // ================= МЕТОД /me (из первого файла) =================

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserEndpoint() {
        try {
            // Получаем текущего аутентифицированного пользователя
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String login = authentication.getName();

            User user = userService.findByLogin(login)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("login", user.getLogin());
            response.put("name", user.getName());
            response.put("surname", user.getSurname());
            response.put("patronymic", user.getPatronymic());
            response.put("role", user.getIdRole().getNameRole());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ================= ОСНОВНЫЕ CRUD МЕТОДЫ =================

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

    // ================= МЕТОДЫ СОЗДАНИЯ (объединенные) =================

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
            if (patronymic == null) {
                patronymic = "";
            }

            // Получаем текущего пользователя (может быть null)
            User currentUser = getCurrentUser();

            // Передаем текущего пользователя в сервис
            User createdUser = userService.createUser(
                    surname, name, patronymic, login, password, roleId, currentUser
            );

            createdUser.setPassword(null);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (RuntimeException e) {
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

            // Извлекаем ID из вложенного объекта idRole (улучшенная обработка из второго файла)
            Map<String, Object> idRole = (Map<String, Object>) payload.get("idRole");
            Integer roleId = null;
            if (idRole != null) {
                Object idObj = idRole.get("id");
                if (idObj instanceof Integer) {
                    roleId = (Integer) idObj;
                } else if (idObj instanceof String) {
                    roleId = Integer.parseInt((String) idObj);
                }
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

            // Получаем текущего пользователя для логирования
            User currentUser = getCurrentUser();

            // Создаем пользователя через сервис
            User createdUser = userService.createUser(
                    surname, name, patronymic, login, password, roleId, currentUser
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

    // ================= МЕТОД РЕГИСТРАЦИИ (из второго файла) =================

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody Map<String, Object> payload) {
        try {
            // Извлекаем все поля из JSON
            String surname = (String) payload.get("surname");
            String name = (String) payload.get("name");
            String patronymic = (String) payload.get("patronymic");
            String login = (String) payload.get("login");
            String password = (String) payload.get("password");

            // Для регистрации обычно роль "Абонент" (id = 2)
            Integer roleId = 2;

            // Валидация
            if (surname == null || surname.trim().isEmpty() ||
                    name == null || name.trim().isEmpty() ||
                    login == null || login.trim().isEmpty() ||
                    password == null || password.trim().isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            if (patronymic == null) {
                patronymic = "";
            }

            // При регистрации нет текущего пользователя, поэтому передаем null
            User createdUser = userService.createUser(
                    surname, name, patronymic, login, password, roleId, null
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

    // ================= МЕТОДЫ ОБНОВЛЕНИЯ И УДАЛЕНИЯ =================

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        Optional<User> existingUser = userService.findById(id);
        if (!existingUser.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            // Получаем текущего пользователя для логирования
            User currentUser = getCurrentUser();

            User updatedUser = userService.update(id, user, currentUser);
            updatedUser.setPassword(null); // Не возвращаем пароль
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        Optional<User> userOpt = userService.findById(id);
        if (!userOpt.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            userService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // ================= ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ =================

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