package com.example.server;

import com.example.server.model.*;
import com.example.server.repository.SubscriberRepository;
import com.example.server.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class SubscriberServiceTest {

    @Mock
    private SubscriberRepository repository;

    @Mock private UserService userService;
    @Mock private PostService postService;
    @Mock private DivisionService divisionService;
    @Mock private BuildingService buildingService;

    @InjectMocks
    private SubscriberService service;

    @Test
    void createSubscriber_success() {

        User user = new User();
        user.setId(1L);

        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(postService.findById(1)).thenReturn(Optional.of(new Post()));
        when(divisionService.findById(1)).thenReturn(Optional.of(new Division()));
        when(buildingService.findById(1)).thenReturn(Optional.of(new Building()));

        when(repository.findByCleanMobilePhoneNumber(any())).thenReturn(List.of());
        when(repository.findByCleanLandlinePhoneNumber(any())).thenReturn(List.of());
        when(repository.findByCleanInternalPhoneNumber(any())).thenReturn(List.of());
        when(repository.findByIdUserId(1L)).thenReturn(List.of());

        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Subscriber result = service.createSubscriber(
                1L,1,1,1,
                null,"101","123","456","+79991234567"
        );

        assertNotNull(result);
        verify(repository).save(any());
    }
}