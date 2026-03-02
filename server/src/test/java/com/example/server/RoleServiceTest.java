package com.example.server;

import com.example.server.model.Role;
import com.example.server.repository.RoleRepository;
import com.example.server.service.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository repository;

    @InjectMocks
    private RoleService service;

    @Test
    void createRole_success() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Role result = service.createRole("ADMIN");

        assertEquals("ADMIN", result.getNameRole());
        verify(repository).save(any());
    }
}