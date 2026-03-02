package com.example.server;

import com.example.server.model.Building;
import com.example.server.repository.BuildingRepository;
import com.example.server.service.BuildingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class BuildingServiceTest {

    @Mock
    private BuildingRepository repository;

    @InjectMocks
    private BuildingService service;

    @Test
    void createBuilding_success() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Building result = service.createBuilding("Main", "Address");

        assertEquals("Main", result.getNameBuilding());
        verify(repository).save(any());
    }
}