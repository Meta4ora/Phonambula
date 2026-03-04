package com.example.server;

import com.example.server.model.Post;
import com.example.server.repository.PostRepository;
import com.example.server.service.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository repository;

    @InjectMocks
    private PostService service;

    // успешное создание должности
    @Test
    void createPost_success() {
        String name = "Программист";
        when(repository.save(any(Post.class))).thenAnswer(i -> i.getArgument(0));

        Post result = service.createPost(name);

        assertNotNull(result);
        assertEquals(name, result.getNamePost());
        verify(repository, times(1)).save(any(Post.class));
    }

    // возвращение списка должностей
    @Test
    void findAll_returnsListOfPosts() {
        Post post1 = new Post("Программист");
        Post post2 = new Post("Тестировщик");
        when(repository.findAll()).thenReturn(Arrays.asList(post1, post2));

        List<Post> result = service.findAll();

        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    // поиск должности по существующему ID
    @Test
    void findById_existingId_returnsPost() {
        Integer id = 1;
        Post post = new Post("Программист");
        post.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(post));

        Optional<Post> result = service.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("Программист", result.get().getNamePost());
        verify(repository, times(1)).findById(id);
    }

    // поиск должности по несуществующему ID
    @Test
    void findById_nonExistingId_returnsEmpty() {
        Integer id = 999;
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Post> result = service.findById(id);

        assertTrue(result.isEmpty());
        verify(repository, times(1)).findById(id);
    }

    // сохранение должности
    @Test
    void save_callsRepositorySave() {
        Post post = new Post("Аналитик");
        when(repository.save(post)).thenReturn(post);

        Post result = service.save(post);

        assertNotNull(result);
        assertEquals("Аналитик", result.getNamePost());
        verify(repository, times(1)).save(post);
    }

    // удаление должности по ID
    @Test
    void deleteById_callsRepositoryDelete() {
        Integer id = 1;
        doNothing().when(repository).deleteById(id);

        service.deleteById(id);

        verify(repository, times(1)).deleteById(id);
    }

    // обновление существующей должности
    @Test
    void update_existingId_updatesAndReturnsPost() {
        Integer id = 1;
        Post updatedPost = new Post("Senior Developer");
        updatedPost.setId(id);
        when(repository.save(any(Post.class))).thenReturn(updatedPost);

        Post result = service.update(id, updatedPost);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Senior Developer", result.getNamePost());
        verify(repository, times(1)).save(updatedPost);
    }
}