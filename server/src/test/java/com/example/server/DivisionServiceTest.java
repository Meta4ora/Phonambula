package com.example.server;

import com.example.server.model.Division;
import com.example.server.repository.DivisionRepository;
import com.example.server.service.DivisionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class DivisionServiceTest {

    @Mock
    private DivisionRepository repository;

    @InjectMocks
    private DivisionService service;

    // успешное создание отдела
    @Test
    void createDivision_success() {
        String name = "IT-отдел";
        when(repository.save(any(Division.class))).thenAnswer(i -> i.getArgument(0));

        Division result = service.createDivision(name);

        assertNotNull(result);
        assertEquals(name, result.getNameDivision());
        verify(repository, times(1)).save(any(Division.class));
    }

    // возвращение списка отделов
    @Test
    void findAll_returnsListOfDivisions() {
        Division div1 = new Division("IT-отдел");
        Division div2 = new Division("Бухгалтерия");
        when(repository.findAll()).thenReturn(Arrays.asList(div1, div2));

        List<Division> result = service.findAll();

        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    // поиск отдела по существующему ID
    @Test
    void findById_existingId_returnsDivision() {
        Integer id = 1;
        Division division = new Division("IT-отдел");
        division.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(division));

        Optional<Division> result = service.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("IT-отдел", result.get().getNameDivision());
        verify(repository, times(1)).findById(id);
    }

    // поиск отдела по несуществующему ID
    @Test
    void findById_nonExistingId_returnsEmpty() {
        Integer id = 999;
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Division> result = service.findById(id);

        assertTrue(result.isEmpty());
        verify(repository, times(1)).findById(id);
    }

    // сохранение отдела
    @Test
    void save_callsRepositorySave() {
        Division division = new Division("Маркетинг");
        when(repository.save(division)).thenReturn(division);

        Division result = service.save(division);

        assertNotNull(result);
        assertEquals("Маркетинг", result.getNameDivision());
        verify(repository, times(1)).save(division);
    }

    // удаление отдела по ID
    @Test
    void deleteById_callsRepositoryDelete() {
        Integer id = 1;
        doNothing().when(repository).deleteById(id);

        service.deleteById(id);

        verify(repository, times(1)).deleteById(id);
    }

    // обновление существующего отдела
    @Test
    void update_existingId_updatesAndReturnsDivision() {
        Integer id = 1;
        Division updatedDivision = new Division("Новый отдел");
        updatedDivision.setId(id);
        when(repository.save(any(Division.class))).thenReturn(updatedDivision);

        Division result = service.update(id, updatedDivision);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Новый отдел", result.getNameDivision());
        verify(repository, times(1)).save(updatedDivision);
    }
}