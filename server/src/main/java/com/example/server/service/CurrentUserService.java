package com.example.server.service;

import com.example.server.model.User;
import com.example.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    @Autowired
    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof String) {
                String login = (String) principal;
                if (!"anonymousUser".equals(login)) {
                    return userRepository.findByLogin(login).orElse(null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getSystemUser() {
        return userRepository.findByLogin("admin").orElse(null);
    }

    public User getActorForLogging() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return currentUser;
        }
        return getSystemUser();
    }
}