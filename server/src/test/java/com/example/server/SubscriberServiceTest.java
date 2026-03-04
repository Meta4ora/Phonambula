package com.example.server;

import com.example.server.model.*;
import com.example.server.repository.SubscriberRepository;
import com.example.server.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
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

    private User createTestUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setLogin("user" + id);
        return user;
    }

    private Post createTestPost(Integer id, String name) {
        Post post = new Post(name);
        post.setId(id);
        return post;
    }

    private Division createTestDivision(Integer id, String name) {
        Division division = new Division(name);
        division.setId(id);
        return division;
    }

    private Building createTestBuilding(Integer id, String name) {
        Building building = new Building(name, "Address");
        building.setId(id);
        return building;
    }

    // успешное создание абонента
    @Test
    void createSubscriber_success() {
        Long userId = 1L;
        Integer postId = 1;
        Integer divisionId = 1;
        Integer buildingId = 1;

        User user = createTestUser(userId);
        Post post = createTestPost(postId, "Программист");
        Division division = createTestDivision(divisionId, "IT-отдел");
        Building building = createTestBuilding(buildingId, "Главный корпус");

        when(userService.findById(userId)).thenReturn(Optional.of(user));
        when(postService.findById(postId)).thenReturn(Optional.of(post));
        when(divisionService.findById(divisionId)).thenReturn(Optional.of(division));
        when(buildingService.findById(buildingId)).thenReturn(Optional.of(building));

        when(repository.findByCleanMobilePhoneNumber(any())).thenReturn(List.of());
        when(repository.findByCleanLandlinePhoneNumber(any())).thenReturn(List.of());
        when(repository.findByCleanInternalPhoneNumber(any())).thenReturn(List.of());
        when(repository.findByIdUserId(userId)).thenReturn(List.of());

        when(repository.save(any(Subscriber.class))).thenAnswer(i -> i.getArgument(0));

        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        Subscriber result = service.createSubscriber(
                userId, postId, divisionId, buildingId,
                birthDate, "101", "123", "456", "+7 (999) 123-45-67"
        );

        assertNotNull(result);
        assertEquals(user, result.getIdUser());
        assertEquals(post, result.getIdPost());
        assertEquals(division, result.getIdDivision());
        assertEquals(building, result.getIdBuilding());
        assertEquals(birthDate, result.getDateBirth());
        assertEquals("101", result.getCabinetNumber());
        assertEquals("123", result.getInternalPhoneNumber());
        assertEquals("456", result.getLandlinePhoneNumber());
        assertEquals("+7 (999) 123-45-67", result.getMobilePhoneNumber());
        verify(repository, times(1)).save(any(Subscriber.class));
    }

    // создание абонента с ненайденным пользователем вызывает исключение
    @Test
    void createSubscriber_shouldThrow_whenUserNotFound() {
        when(userService.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.createSubscriber(999L, 1, 1, 1, null, "101", "123", "456", "+7 (999) 123-45-67")
        );
        verify(repository, never()).save(any());
    }

    // создание абонента с ненайденной должностью вызывает исключение
    @Test
    void createSubscriber_shouldThrow_whenPostNotFound() {
        when(userService.findById(1L)).thenReturn(Optional.of(createTestUser(1L)));
        when(postService.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.createSubscriber(1L, 999, 1, 1, null, "101", "123", "456", "+7 (999) 123-45-67")
        );
        verify(repository, never()).save(any());
    }

    // создание абонента с ненайденным отделом вызывает исключение
    @Test
    void createSubscriber_shouldThrow_whenDivisionNotFound() {
        when(userService.findById(1L)).thenReturn(Optional.of(createTestUser(1L)));
        when(postService.findById(1)).thenReturn(Optional.of(createTestPost(1, "Программист")));
        when(divisionService.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.createSubscriber(1L, 1, 999, 1, null, "101", "123", "456", "+7 (999) 123-45-67")
        );
        verify(repository, never()).save(any());
    }

    // создание абонента с ненайденным зданием вызывает исключение
    @Test
    void createSubscriber_shouldThrow_whenBuildingNotFound() {
        when(userService.findById(1L)).thenReturn(Optional.of(createTestUser(1L)));
        when(postService.findById(1)).thenReturn(Optional.of(createTestPost(1, "Программист")));
        when(divisionService.findById(1)).thenReturn(Optional.of(createTestDivision(1, "IT")));
        when(buildingService.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.createSubscriber(1L, 1, 1, 999, null, "101", "123", "456", "+7 (999) 123-45-67")
        );
        verify(repository, never()).save(any());
    }

    // создание абонента с существующим телефоном вызывает исключение
    @Test
    void createSubscriber_shouldThrow_whenPhoneAlreadyExists() {
        Long userId = 1L;
        Integer postId = 1;
        Integer divisionId = 1;
        Integer buildingId = 1;

        when(userService.findById(userId)).thenReturn(Optional.of(createTestUser(userId)));
        when(postService.findById(postId)).thenReturn(Optional.of(createTestPost(postId, "Программист")));
        when(divisionService.findById(divisionId)).thenReturn(Optional.of(createTestDivision(divisionId, "IT")));
        when(buildingService.findById(buildingId)).thenReturn(Optional.of(createTestBuilding(buildingId, "Главный")));

        Subscriber existingSubscriber = new Subscriber();
        when(repository.findByCleanMobilePhoneNumber("79991234567")).thenReturn(List.of(existingSubscriber));

        assertThrows(RuntimeException.class, () ->
                service.createSubscriber(1L, 1, 1, 1, null, "101", "123", "456", "+7 (999) 123-45-67")
        );
        verify(repository, never()).save(any());
    }

    // возвращение списка абонентов
    @Test
    void findAll_returnsListOfSubscribers() {
        Subscriber sub1 = new Subscriber();
        Subscriber sub2 = new Subscriber();
        when(repository.findAll()).thenReturn(Arrays.asList(sub1, sub2));

        List<Subscriber> result = service.findAll();

        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    // поиск абонента по существующему ID
    @Test
    void findById_existingId_returnsSubscriber() {
        Long id = 1L;
        Subscriber subscriber = new Subscriber();
        subscriber.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(subscriber));

        Optional<Subscriber> result = service.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(repository, times(1)).findById(id);
    }

    // поиск абонента по несуществующему ID
    @Test
    void findById_nonExistingId_returnsEmpty() {
        Long id = 999L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Subscriber> result = service.findById(id);

        assertTrue(result.isEmpty());
        verify(repository, times(1)).findById(id);
    }

    // поиск абонентов по ID отдела
    @Test
    void findByDivisionId_returnsFilteredList() {
        Integer divisionId = 1;
        Subscriber sub1 = new Subscriber();
        Subscriber sub2 = new Subscriber();
        when(repository.findByIdDivisionId(divisionId)).thenReturn(Arrays.asList(sub1, sub2));

        List<Subscriber> result = service.findByDivisionId(divisionId);

        assertEquals(2, result.size());
        verify(repository, times(1)).findByIdDivisionId(divisionId);
    }

    // поиск абонентов по ID здания
    @Test
    void findByBuildingId_returnsFilteredList() {
        Integer buildingId = 1;
        Subscriber sub1 = new Subscriber();
        when(repository.findByIdBuildingId(buildingId)).thenReturn(List.of(sub1));

        List<Subscriber> result = service.findByBuildingId(buildingId);

        assertEquals(1, result.size());
        verify(repository, times(1)).findByIdBuildingId(buildingId);
    }

    // поиск абонентов по ID должности
    @Test
    void findByPostId_returnsFilteredList() {
        Integer postId = 1;
        Subscriber sub1 = new Subscriber();
        when(repository.findByIdPostId(postId)).thenReturn(List.of(sub1));

        List<Subscriber> result = service.findByPostId(postId);

        assertEquals(1, result.size());
        verify(repository, times(1)).findByIdPostId(postId);
    }

    // поиск абонентов по ID пользователя
    @Test
    void findByUserId_returnsFilteredList() {
        Long userId = 1L;
        Subscriber sub1 = new Subscriber();
        when(repository.findByIdUserId(userId)).thenReturn(List.of(sub1));

        List<Subscriber> result = service.findByUserId(userId);

        assertEquals(1, result.size());
        verify(repository, times(1)).findByIdUserId(userId);
    }

    // поиск абонентов по поисковому запросу
    @Test
    void search_withTerm_returnsResults() {
        String searchTerm = "Иван";
        Subscriber sub1 = new Subscriber();
        when(repository.searchSubscribers(searchTerm)).thenReturn(List.of(sub1));

        List<Subscriber> result = service.search(searchTerm);

        assertEquals(1, result.size());
        verify(repository, times(1)).searchSubscribers(searchTerm);
    }

    // поиск с пустым запросом возвращает всех абонентов
    @Test
    void search_withEmptyTerm_returnsAll() {
        when(repository.findAll()).thenReturn(List.of(new Subscriber(), new Subscriber()));

        List<Subscriber> result = service.search("");

        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
        verify(repository, never()).searchSubscribers(any());
    }

    // сохранение абонента с проверкой уникальности телефона
    @Test
    void save_validatesPhoneUniqueness() {
        Subscriber subscriber = new Subscriber();
        subscriber.setMobilePhoneNumber("+7 (999) 123-45-67");

        when(repository.findByCleanMobilePhoneNumber("79991234567")).thenReturn(List.of());

        service.save(subscriber);

        verify(repository, times(1)).save(subscriber);
    }

    // сохранение абонента с неуникальным телефоном вызывает исключение
    @Test
    void save_shouldThrow_whenPhoneNotUnique() {
        Subscriber subscriber = new Subscriber();
        subscriber.setMobilePhoneNumber("+7 (999) 123-45-67");

        when(repository.findByCleanMobilePhoneNumber("79991234567"))
                .thenReturn(List.of(new Subscriber()));

        assertThrows(RuntimeException.class, () -> service.save(subscriber));
        verify(repository, never()).save(any());
    }

    // удаление абонента по ID
    @Test
    void deleteById_callsRepositoryDelete() {
        Long id = 1L;
        doNothing().when(repository).deleteById(id);

        service.deleteById(id);

        verify(repository, times(1)).deleteById(id);
    }
}