package com.example.server.service;

import com.example.server.model.Building;
import com.example.server.model.User;
import com.example.server.repository.BuildingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserService currentUserService;

    @Autowired
    public BuildingService(BuildingRepository buildingRepository,
                           AuditLogService auditLogService,
                           CurrentUserService currentUserService) {
        this.buildingRepository = buildingRepository;
        this.auditLogService = auditLogService;
        this.currentUserService = currentUserService;
    }

    public List<Building> findAll() {
        return buildingRepository.findAll();
    }

    public Optional<Building> findById(Integer id) {
        return buildingRepository.findById(id);
    }

    @Transactional
    public Building createBuilding(String nameBuilding, String address) {
        Building building = new Building(nameBuilding, address);
        Building savedBuilding = buildingRepository.save(building);

        // Логирование - используем Map для afterData
        try {
            Map<String, Object> afterData = Map.of(
                    "id", savedBuilding.getId(),
                    "name", savedBuilding.getNameBuilding(),
                    "address", savedBuilding.getAddress()
            );

            auditLogService.createAuditLog(
                    currentUserService.getActorForLogging(),
                    "INSERT",
                    "buildings",
                    savedBuilding.getId(),
                    null,  // beforeData = null
                    afterData
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedBuilding;
    }

    @Transactional
    public Building save(Building building) {
        boolean isNew = building.getId() == null;
        Map<String, Object> beforeData = null;

        if (!isNew) {
            Optional<Building> existing = findById(building.getId());
            if (existing.isPresent()) {
                beforeData = Map.of(
                        "id", existing.get().getId(),
                        "name", existing.get().getNameBuilding(),
                        "address", existing.get().getAddress()
                );
            }
        }

        Building savedBuilding = buildingRepository.save(building);

        // Логирование
        try {
            Map<String, Object> afterData = Map.of(
                    "id", savedBuilding.getId(),
                    "name", savedBuilding.getNameBuilding(),
                    "address", savedBuilding.getAddress()
            );

            auditLogService.createAuditLog(
                    currentUserService.getActorForLogging(),
                    isNew ? "INSERT" : "UPDATE",
                    "buildings",
                    savedBuilding.getId(),
                    beforeData,
                    afterData
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedBuilding;
    }

    @Transactional
    public void deleteById(Integer id) {
        Optional<Building> buildingOpt = findById(id);
        if (buildingOpt.isPresent()) {
            Building building = buildingOpt.get();

            // Логирование до удаления
            try {
                Map<String, Object> beforeData = Map.of(
                        "id", building.getId(),
                        "name", building.getNameBuilding(),
                        "address", building.getAddress()
                );

                auditLogService.createAuditLog(
                        currentUserService.getActorForLogging(),
                        "DELETE",
                        "buildings",
                        id,
                        beforeData,
                        null
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            buildingRepository.deleteById(id);
        }
    }

    @Transactional
    public Building update(Integer id, Building building) {
        building.setId(id);
        return save(building);
    }
}