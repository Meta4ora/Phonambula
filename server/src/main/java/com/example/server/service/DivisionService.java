package com.example.server.service;

import com.example.server.model.Division;
import com.example.server.model.User;
import com.example.server.repository.DivisionRepository;
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
public class DivisionService {

    private final DivisionRepository divisionRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserService currentUserService;

    @Autowired
    public DivisionService(DivisionRepository divisionRepository,
                           AuditLogService auditLogService,
                           CurrentUserService currentUserService) {
        this.divisionRepository = divisionRepository;
        this.auditLogService = auditLogService;
        this.currentUserService = currentUserService;
    }

    public List<Division> findAll() {
        return divisionRepository.findAll();
    }

    public Optional<Division> findById(Integer id) {
        return divisionRepository.findById(id);
    }

    @Transactional
    public Division createDivision(String nameDivision) {
        Division division = new Division(nameDivision);
        Division savedDivision = divisionRepository.save(division);

        // Логирование
        try {
            Map<String, Object> divisionData = Map.of(
                    "id", savedDivision.getId(),
                    "name", savedDivision.getNameDivision()
            );

            auditLogService.createAuditLog(
                    currentUserService.getActorForLogging(),
                    "INSERT",
                    "divisions",
                    savedDivision.getId(),
                    null,
                    divisionData
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedDivision;
    }

    @Transactional
    public Division save(Division division) {
        boolean isNew = division.getId() == null;
        Map<String, Object> beforeData = null;

        if (!isNew) {
            Optional<Division> existing = findById(division.getId());
            if (existing.isPresent()) {
                beforeData = Map.of(
                        "id", existing.get().getId(),
                        "name", existing.get().getNameDivision()
                );
            }
        }

        Division savedDivision = divisionRepository.save(division);

        // Логирование
        try {
            Map<String, Object> afterData = Map.of(
                    "id", savedDivision.getId(),
                    "name", savedDivision.getNameDivision()
            );

            auditLogService.createAuditLog(
                    currentUserService.getActorForLogging(),
                    isNew ? "INSERT" : "UPDATE",
                    "divisions",
                    savedDivision.getId(),
                    beforeData,
                    afterData
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedDivision;
    }

    @Transactional
    public void deleteById(Integer id) {
        Optional<Division> divisionOpt = findById(id);
        if (divisionOpt.isPresent()) {
            Division division = divisionOpt.get();

            // Логирование до удаления
            try {
                Map<String, Object> beforeData = Map.of(
                        "id", division.getId(),
                        "name", division.getNameDivision()
                );

                auditLogService.createAuditLog(
                        currentUserService.getActorForLogging(),
                        "DELETE",
                        "divisions",
                        id,
                        beforeData,
                        null
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            divisionRepository.deleteById(id);
        }
    }

    @Transactional
    public Division update(Integer id, Division division) {
        division.setId(id);
        return save(division);
    }
}