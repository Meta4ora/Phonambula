package com.example.server.service;

import com.example.server.model.Role;
import com.example.server.model.User;
import com.example.server.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleService roleService,
                       PasswordEncoder passwordEncoder,
                       AuditLogService auditLogService,
                       CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.currentUserService = currentUserService;
    }

    // ================= БАЗОВЫЕ МЕТОДЫ =================

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // ================= МЕТОДЫ СОЗДАНИЯ =================

    @Transactional
    public User createUser(String surname, String name, String patronymic,
                           String login, String password, Integer roleId) {
        return createUser(surname, name, patronymic, login, password, roleId, null);
    }

    @Transactional
    public User createUser(String surname, String name, String patronymic,
                           String login, String password, Integer roleId, User providedActor) {

        // Проверка на существующий логин
        if (userRepository.existsByLogin(login)) {
            throw new RuntimeException("Пользователь с логином '" + login + "' уже существует");
        }

        // Находим роль по ID
        Role role = roleService.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Роль с ID " + roleId + " не найдена"));

        // Создаем пользователя
        User user = new User(surname, name, patronymic, login, role);
        user.setPassword(passwordEncoder.encode(password));

        User savedUser = userRepository.save(user);

        // Определяем, кто выполняет действие
        User actor = providedActor != null ? providedActor : currentUserService.getActorForLogging();

        // Логируем создание пользователя
        try {
            Map<String, Object> userData = Map.of(
                    "id", savedUser.getId(),
                    "login", savedUser.getLogin(),
                    "name", savedUser.getName(),
                    "surname", savedUser.getSurname(),
                    "role", savedUser.getIdRole() != null ? savedUser.getIdRole().getNameRole() : null
            );

            auditLogService.createAuditLog(
                    actor,
                    "INSERT",
                    "users",
                    savedUser.getId().intValue(),
                    null,  // до создания нет данных
                    userData
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedUser;
    }

    // ================= МЕТОДЫ СОХРАНЕНИЯ =================

    @Transactional
    public User save(User user) {
        return save(user, null);
    }

    @Transactional
    public User save(User user, User providedActor) {
        boolean isNew = user.getId() == null;

        // Проверка на существующий логин (если это новый пользователь)
        if (isNew && userRepository.existsByLogin(user.getLogin())) {
            throw new RuntimeException("Пользователь с логином '" + user.getLogin() + "' уже существует");
        }

        User savedUser;
        String beforeJson = null;

        if (!isNew) {
            // Для существующего пользователя сохраняем состояние до изменений
            Optional<User> existingUser = findById(user.getId());
            if (existingUser.isPresent()) {
                try {
                    beforeJson = objectMapper.writeValueAsString(Map.of(
                            "id", existingUser.get().getId(),
                            "login", existingUser.get().getLogin(),
                            "name", existingUser.get().getName(),
                            "surname", existingUser.get().getSurname(),
                            "role", existingUser.get().getIdRole() != null ? existingUser.get().getIdRole().getNameRole() : null
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Хешируем пароль, если он не захеширован
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        savedUser = userRepository.save(user);

        // Определяем, кто выполняет действие
        User actor = providedActor != null ? providedActor : currentUserService.getActorForLogging();

        // Логируем действие
        try {
            String afterJson = objectMapper.writeValueAsString(Map.of(
                    "id", savedUser.getId(),
                    "login", savedUser.getLogin(),
                    "name", savedUser.getName(),
                    "surname", savedUser.getSurname(),
                    "role", savedUser.getIdRole() != null ? savedUser.getIdRole().getNameRole() : null
            ));

            auditLogService.createAuditLog(
                    actor,
                    isNew ? "INSERT" : "UPDATE",
                    "users",
                    savedUser.getId().intValue(),
                    beforeJson,
                    afterJson
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedUser;
    }

    // ================= МЕТОДЫ УДАЛЕНИЯ =================

    @Transactional
    public void deleteById(Long id) {
        deleteById(id, null);
    }

    @Transactional
    public void deleteById(Long id, User providedActor) {
        Optional<User> userOpt = findById(id);
        if (userOpt.isPresent()) {
            User userToDelete = userOpt.get();

            // Определяем, кто выполняет действие
            User actor = providedActor != null ? providedActor : currentUserService.getActorForLogging();

            // Логируем удаление до фактического удаления
            try {
                String beforeJson = objectMapper.writeValueAsString(Map.of(
                        "id", userToDelete.getId(),
                        "login", userToDelete.getLogin(),
                        "name", userToDelete.getName(),
                        "surname", userToDelete.getSurname(),
                        "role", userToDelete.getIdRole() != null ? userToDelete.getIdRole().getNameRole() : null
                ));

                auditLogService.createAuditLog(
                        actor,
                        "DELETE",
                        "users",
                        id.intValue(),
                        beforeJson,
                        null
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            userRepository.deleteById(id);
        }
    }

    // ================= МЕТОДЫ ОБНОВЛЕНИЯ =================

    @Transactional
    public User update(Long id, User user) {
        return update(id, user, null);
    }

    @Transactional
    public User update(Long id, User user, User providedActor) {
        User existingUser = findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + id + " не найден"));

        // Сохраняем состояние до изменений для лога
        String beforeJson = null;
        try {
            beforeJson = objectMapper.writeValueAsString(Map.of(
                    "id", existingUser.getId(),
                    "login", existingUser.getLogin(),
                    "name", existingUser.getName(),
                    "surname", existingUser.getSurname(),
                    "role", existingUser.getIdRole() != null ? existingUser.getIdRole().getNameRole() : null
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Обновляем поля
        existingUser.setSurname(user.getSurname());
        existingUser.setName(user.getName());
        existingUser.setPatronymic(user.getPatronymic());
        existingUser.setLogin(user.getLogin());
        existingUser.setIdRole(user.getIdRole());

        // Если передан новый пароль и он отличается от текущего
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            // Проверяем, что пароль новый (не захеширован)
            if (!user.getPassword().startsWith("$2a")) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            // Если пароль уже захеширован, оставляем как есть
        }

        User updatedUser = userRepository.save(existingUser);

        // Определяем, кто выполняет действие
        User actor = providedActor != null ? providedActor : currentUserService.getActorForLogging();

        // Логируем обновление
        try {
            String afterJson = objectMapper.writeValueAsString(Map.of(
                    "id", updatedUser.getId(),
                    "login", updatedUser.getLogin(),
                    "name", updatedUser.getName(),
                    "surname", updatedUser.getSurname(),
                    "role", updatedUser.getIdRole() != null ? updatedUser.getIdRole().getNameRole() : null
            ));

            auditLogService.createAuditLog(
                    actor,
                    "UPDATE",
                    "users",
                    id.intValue(),
                    beforeJson,
                    afterJson
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return updatedUser;
    }

    // ================= МЕТОДЫ АУТЕНТИФИКАЦИИ =================

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOpt = findByLogin(username);

        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("Пользователь не найден: " + username);
        }

        User user = userOpt.get();

        // Преобразуем вашу модель User в UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getLogin(),
                user.getPassword(),
                true, true, true, true,   // accountNonExpired, credentialsNonExpired, enabled, accountNonLocked
                getAuthorities(user.getIdRole())
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Role role) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.getNameRole()));
    }

    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public boolean isLoginExists(String login) {
        return userRepository.existsByLogin(login);
    }

    /**
     * Метод для аутентификации пользователя
     */
    public Optional<User> authenticate(String login, String rawPassword) {
        Optional<User> userOpt = userRepository.findByLogin(login);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(rawPassword, user.getPassword())) {
                return userOpt;
            }
        }

        return Optional.empty();
    }

    /**
     * Проверяет существует ли пользователь с таким логином и паролем
     */
    public boolean checkUserExists(String login, String rawPassword) {
        Optional<User> userOpt = userRepository.findByLogin(login);
        return userOpt.filter(user -> passwordEncoder.matches(rawPassword, user.getPassword())).isPresent();
    }
}