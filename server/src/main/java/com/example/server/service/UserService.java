package com.example.server.service;

import com.example.server.model.Role;
import com.example.server.model.User;
import com.example.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;

    @Autowired
    public UserService(UserRepository userRepository, RoleService roleService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
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
        // Находим роль по ID
        Role role = roleService.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Роль с ID " + roleId + " не найдена"));

        // Используем конструктор
        User user = new User(surname, name, patronymic, login, password, role);
        return userRepository.save(user);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public User update(Long id, User user) {
        user.setId(id);
        return userRepository.save(user);
    }

    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public boolean isLoginExists(String login) {
        return userRepository.existsByLogin(login);
    }
}