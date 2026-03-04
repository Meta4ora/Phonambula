package com.example.server;

import com.example.server.model.Role;
import com.example.server.repository.RoleRepository;
import com.example.server.service.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository repository;

    @InjectMocks
    private RoleService service;

    // успешное создание роли
    @Test
    void createRole_success() {
        String name = "ADMIN";
        when(repository.save(any(Role.class))).thenAnswer(i -> i.getArgument(0));

        Role result = service.createRole(name);

        assertNotNull(result);
        assertEquals(name, result.getNameRole());
        verify(repository, times(1)).save(any(Role.class));
    }

    // возвращение списка ролей
    @Test
    void findAll_returnsListOfRoles() {
        Role role1 = new Role("ADMIN");
        Role role2 = new Role("USER");
        when(repository.findAll()).thenReturn(Arrays.asList(role1, role2));

        List<Role> result = service.findAll();

        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    // поиск роли по существующему ID
    @Test
    void findById_existingId_returnsRole() {
        Integer id = 1;
        Role role = new Role("ADMIN");
        role.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(role));

        Optional<Role> result = service.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("ADMIN", result.get().getNameRole());
        verify(repository, times(1)).findById(id);
    }

    // поиск роли по несуществующему ID
    @Test
    void findById_nonExistingId_returnsEmpty() {
        Integer id = 999;
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Role> result = service.findById(id);

        assertTrue(result.isEmpty());
        verify(repository, times(1)).findById(id);
    }

    // сохранение роли
    @Test
    void save_callsRepositorySave() {
        Role role = new Role("MODERATOR");
        when(repository.save(role)).thenReturn(role);

        Role result = service.save(role);

        assertNotNull(result);
        assertEquals("MODERATOR", result.getNameRole());
        verify(repository, times(1)).save(role);
    }

    // удаление роли по ID
    @Test
    void deleteById_callsRepositoryDelete() {
        Integer id = 1;
        doNothing().when(repository).deleteById(id);

        service.deleteById(id);

        verify(repository, times(1)).deleteById(id);
    }

    // обновление существующей роли
    @Test
    void update_existingId_updatesAndReturnsRole() {
        Integer id = 1;
        Role updatedRole = new Role("SUPER_ADMIN");
        updatedRole.setId(id);
        when(repository.save(any(Role.class))).thenReturn(updatedRole);

        Role result = service.update(id, updatedRole);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("SUPER_ADMIN", result.getNameRole());
        verify(repository, times(1)).save(updatedRole);
    }
}