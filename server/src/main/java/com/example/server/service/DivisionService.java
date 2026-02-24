package com.example.server.service;

import com.example.server.model.Division;
import com.example.server.model.Role;
import com.example.server.repository.DivisionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class DivisionService {

    private final DivisionRepository divisionRepository;

    @Autowired
    public DivisionService(DivisionRepository divisionRepository) {
        this.divisionRepository = divisionRepository;
    }

    public List<Division> findAll() {
        return divisionRepository.findAll();
    }

    public Optional<Division> findById(Integer id) {
        return divisionRepository.findById(id);
    }


    @Transactional
    public Division createDivision(String nameDivision) {
        // Используем конструктор
        Division division = new Division(nameDivision);
        return divisionRepository.save(division);
    }

    @Transactional
    public Division save(Division division) {
        return divisionRepository.save(division);
    }

    @Transactional
    public void deleteById(Integer id) {
        divisionRepository.deleteById(id);
    }

    @Transactional
    public Division update(Integer id, Division division) {
        division.setId(id);
        return divisionRepository.save(division);
    }
}