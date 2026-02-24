package com.example.server.service;

import com.example.server.model.Building;
import com.example.server.model.Post;
import com.example.server.repository.BuildingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class BuildingService {

    private final BuildingRepository buildingRepository;

    @Autowired
    public BuildingService(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    public List<Building> findAll() {
        return buildingRepository.findAll();
    }

    public Optional<Building> findById(Integer id) {
        return buildingRepository.findById(id);
    }

    @Transactional
    public Building createBuilding(String nameBuilding, String address) {
        // Используем конструктор
        Building building = new Building(nameBuilding, address);
        return buildingRepository.save(building);
    }

    @Transactional
    public Building save(Building building) {
        return buildingRepository.save(building);
    }

    @Transactional
    public void deleteById(Integer id) {
        buildingRepository.deleteById(id);
    }

    @Transactional
    public Building update(Integer id, Building building) {
        building.setId(id);
        return buildingRepository.save(building);
    }
}