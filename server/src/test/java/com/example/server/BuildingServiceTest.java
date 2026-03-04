package com.example.server;

import com.example.server.model.Building;
import com.example.server.repository.BuildingRepository;
import com.example.server.service.BuildingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class BuildingServiceTest {

    @Mock
    private BuildingRepository repository;

    @InjectMocks
    private BuildingService service;

    // успешное создание здания
    @Test
    void createBuilding_success() {
        String name = "Главный корпус";
        String address = "ул. Ленина, 1";
        // создаем фиктивный объект (mock) репозитория
        when(repository.save(any(Building.class))).thenAnswer(i -> i.getArgument(0));

        Building result = service.createBuilding(name, address);

        assertNotNull(result);
        assertEquals(name, result.getNameBuilding());
        assertEquals(address, result.getAddress());
        // проверка взаимодействия с заглушкой
        verify(repository, times(1)).save(any(Building.class));
    }

    // возвращение списка зданий
    @Test
    void findAll_returnsListOfBuildings() {
        Building building1 = new Building("Корпус А", "ул. Варшавская, 5А");
        Building building2 = new Building("Корпус Б", "ул. Варшавская, 5Б");
        when(repository.findAll()).thenReturn(Arrays.asList(building1, building2));

        List<Building> result = service.findAll();

        assertEquals(2, result.size());
        // ожидаем ровно один вызов метода
        verify(repository, times(1)).findAll();
    }

    // поиск здания по существующему ID
    @Test
    void findById_existingId_returnsBuilding() {
        Integer id = 1;
        Building building = new Building("Главный корпус", "ул. Варшавская, 5А");
        building.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(building));

        Optional<Building> result = service.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("Главный корпус", result.get().getNameBuilding());
        verify(repository, times(1)).findById(id);
    }

    // поиск здания по несуществующему ID
    @Test
    void findById_nonExistingId_returnsEmpty() {
        Integer id = 999;
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Building> result = service.findById(id);

        assertTrue(result.isEmpty());
        verify(repository, times(1)).findById(id);
    }

    // сохранение здания
    @Test
    void save_callsRepositorySave() {
        Building building = new Building("Новый корпус", "ул. Новая, 10");
        when(repository.save(building)).thenReturn(building);

        Building result = service.save(building);

        assertNotNull(result);
        assertEquals("Новый корпус", result.getNameBuilding());
        verify(repository, times(1)).save(building);
    }

    // удаление здания по ID
    @Test
    void deleteById_callsRepositoryDelete() {
        Integer id = 1;
        doNothing().when(repository).deleteById(id);

        service.deleteById(id);

        verify(repository, times(1)).deleteById(id);
    }

    // обновление существующего здания
    @Test
    void update_existingId_updatesAndReturnsBuilding() {
        Integer id = 1;
        Building updatedBuilding = new Building("Новое название", "Новый адрес");
        updatedBuilding.setId(id);
        when(repository.save(any(Building.class))).thenReturn(updatedBuilding);

        Building result = service.update(id, updatedBuilding);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Новое название", result.getNameBuilding());
        assertEquals("Новый адрес", result.getAddress());
        verify(repository, times(1)).save(updatedBuilding);
    }

    // создание здания с пустым названием - вызывается исключение
    @Test
    void createBuilding_withEmptyName_throwsException() {
        when(repository.save(any(Building.class))).thenThrow(new IllegalArgumentException("Название не может быть пустым"));

        assertThrows(IllegalArgumentException.class, () -> {
            service.createBuilding("", "Адрес");
        });
    }
}