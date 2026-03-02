package com.example.server;

import com.example.server.model.Role;
import com.example.server.model.User;
import com.example.server.repository.UserRepository;
import com.example.server.service.RoleService;
import com.example.server.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_success() {
        Role role = new Role("ADMIN");

        when(userRepository.existsByLogin("admin")).thenReturn(false);
        when(roleService.findById(1)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("1234")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User user = userService.createUser(
                "Ivanov","Ivan","Ivanovich",
                "admin","1234",1
        );

        assertEquals("admin", user.getLogin());
        verify(passwordEncoder).encode("1234");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_shouldThrow_ifLoginExists() {
        when(userRepository.existsByLogin("admin")).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                userService.createUser("a","b","c","admin","1234",1)
        );
    }
}