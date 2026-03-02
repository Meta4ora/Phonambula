package com.example.server;

import com.example.server.model.Post;
import com.example.server.repository.PostRepository;
import com.example.server.service.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository repository;

    @InjectMocks
    private PostService service;

    @Test
    void createPost_success() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Post result = service.createPost("Manager");

        assertEquals("Manager", result.getNamePost());
        verify(repository).save(any());
    }
}