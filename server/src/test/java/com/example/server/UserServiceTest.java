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

import java.util.Arrays;
import java.util.List;
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

    // успешное создание пользователя
    @Test
    void createUser_success() {
        Role role = new Role("ADMIN");
        role.setId(1);

        when(userRepository.existsByLogin("admin")).thenReturn(false);
        when(roleService.findById(1)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("1234")).thenReturn("encoded123");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User user = userService.createUser(
                "Иванов", "Иван", "Иванович",
                "admin", "1234", 1
        );

        assertNotNull(user);
        assertEquals("admin", user.getLogin());
        assertEquals("encoded123", user.getPassword());
        assertEquals(role, user.getIdRole());
        verify(passwordEncoder, times(1)).encode("1234");
        verify(userRepository, times(1)).save(any(User.class));
    }

    // создание пользователя с существующим логином вызывает исключение
    @Test
    void createUser_shouldThrow_whenLoginExists() {
        when(userRepository.existsByLogin("admin")).thenReturn(true);

        assertThrows(RuntimeException.class, () ->
                userService.createUser("Иванов", "Иван", "Иванович",
                        "admin", "1234", 1)
        );
        verify(userRepository, never()).save(any());
    }

    // создание пользователя с несуществующей ролью вызывает исключение
    @Test
    void createUser_shouldThrow_whenRoleNotFound() {
        when(userRepository.existsByLogin("admin")).thenReturn(false);
        when(roleService.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                userService.createUser("Иванов", "Иван", "Иванович",
                        "admin", "1234", 999)
        );
        verify(userRepository, never()).save(any());
    }

    // возвращение списка пользователей
    @Test
    void findAll_returnsListOfUsers() {
        User user1 = new User();
        User user2 = new User();
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> result = userService.findAll();

        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    // поиск пользователя по существующему ID
    @Test
    void findById_existingId_returnsUser() {
        Long id = 1L;
        User user = new User();
        user.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(userRepository, times(1)).findById(id);
    }

    // поиск пользователя по несуществующему ID
    @Test
    void findById_nonExistingId_returnsEmpty() {
        Long id = 999L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(id);

        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findById(id);
    }

    // поиск пользователя по существующему логину
    @Test
    void findByLogin_existingLogin_returnsUser() {
        String login = "admin";
        User user = new User();
        user.setLogin(login);
        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByLogin(login);

        assertTrue(result.isPresent());
        assertEquals(login, result.get().getLogin());
        verify(userRepository, times(1)).findByLogin(login);
    }

    // успешная аутентификация пользователя
    @Test
    void authenticate_success() {
        String login = "admin";
        String rawPassword = "1234";
        User user = new User();
        user.setLogin(login);
        user.setPassword("encoded123");

        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, "encoded123")).thenReturn(true);

        Optional<User> result = userService.authenticate(login, rawPassword);

        assertTrue(result.isPresent());
        verify(passwordEncoder, times(1)).matches(rawPassword, "encoded123");
    }

    // аутентификация с неверным паролем возвращает пустой результат
    @Test
    void authenticate_wrongPassword_returnsEmpty() {
        String login = "admin";
        String rawPassword = "wrong";
        User user = new User();
        user.setLogin(login);
        user.setPassword("encoded123");

        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, "encoded123")).thenReturn(false);

        Optional<User> result = userService.authenticate(login, rawPassword);

        assertTrue(result.isEmpty());
        verify(passwordEncoder, times(1)).matches(rawPassword, "encoded123");
    }

    // проверка существования пользователя с валидными данными возвращает true
    @Test
    void checkUserExists_returnsTrue_whenCredentialsValid() {
        String login = "admin";
        String rawPassword = "1234";
        User user = new User();
        user.setLogin(login);
        user.setPassword("encoded123");

        when(userRepository.findByLogin(login)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(rawPassword, "encoded123")).thenReturn(true);

        boolean result = userService.checkUserExists(login, rawPassword);

        assertTrue(result);
    }

    // обновление существующего пользователя
    @Test
    void update_existingId_updatesUser() {
        Long id = 1L;
        Role role = new Role("ADMIN");
        role.setId(1);

        User existingUser = new User();
        existingUser.setId(id);
        existingUser.setLogin("oldLogin");

        User updatedUser = new User();
        updatedUser.setSurname("Петров");
        updatedUser.setName("Петр");
        updatedUser.setPatronymic("Петрович");
        updatedUser.setLogin("newLogin");
        updatedUser.setIdRole(role);
        updatedUser.setPassword("newPassword");

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNew");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.update(id, updatedUser);

        assertEquals("Петров", result.getSurname());
        assertEquals("newLogin", result.getLogin());
        assertEquals("encodedNew", result.getPassword());
        verify(userRepository, times(1)).save(existingUser);
    }

    // удаление пользователя по ID
    @Test
    void deleteById_callsRepositoryDelete() {
        Long id = 1L;
        doNothing().when(userRepository).deleteById(id);

        userService.deleteById(id);

        verify(userRepository, times(1)).deleteById(id);
    }
}