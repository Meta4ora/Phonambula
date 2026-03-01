package com.example.server.service;

import com.example.server.model.Role;
import com.example.server.model.User;
import com.example.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleService roleService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User createUser(String surname, String name, String patronymic,
                           String login, String password, Integer roleId) {

        // Проверка на существующий логин
        if (userRepository.existsByLogin(login)) {
            throw new RuntimeException("Пользователь с логином '" + login + "' уже существует");
        }

        // Находим роль по ID
        Role role = roleService.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Роль с ID " + roleId + " не найдена"));

        // Создаем пользователя без пароля
        User user = new User(surname, name, patronymic, login, role);

        // Хешируем и устанавливаем пароль отдельно
        user.setPassword(passwordEncoder.encode(password));

        return userRepository.save(user);
    }

    @Transactional
    public User save(User user) {
        // Проверка на существующий логин (если это новый пользователь)
        if (user.getId() == null && userRepository.existsByLogin(user.getLogin())) {
            throw new RuntimeException("Пользователь с логином '" + user.getLogin() + "' уже существует");
        }

        // Хешируем пароль, если он не захеширован
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public User update(Long id, User user) {
        User existingUser = findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + id + " не найден"));

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
            // Если пароль уже захеширован, оставляем как есть (обычно не должно происходить)
        }

        return userRepository.save(existingUser);
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

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return passwordEncoder.matches(rawPassword, user.getPassword());
        }

        return false;
    }
}