package com.example.server;

import com.example.server.model.Division;
import com.example.server.repository.DivisionRepository;
import com.example.server.service.DivisionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class DivisionServiceTest {

    @Mock
    private DivisionRepository repository;

    @InjectMocks
    private DivisionService service;

    @Test
    void createDivision_success() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Division result = service.createDivision("IT");

        assertEquals("IT", result.getNameDivision());
        verify(repository).save(any());
    }
}