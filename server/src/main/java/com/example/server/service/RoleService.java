package com.example.server.service;

import com.example.server.model.Role;
import com.example.server.model.User;
import com.example.server.repository.RoleRepository;
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
public class RoleService {

    private final RoleRepository roleRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserService currentUserService;

    @Autowired
    public RoleService(RoleRepository roleRepository,
                       AuditLogService auditLogService,
                       CurrentUserService currentUserService) {
        this.roleRepository = roleRepository;
        this.auditLogService = auditLogService;
        this.currentUserService = currentUserService;
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Optional<Role> findById(Integer id) {
        return roleRepository.findById(id);
    }

    @Transactional
    public Role createRole(String nameRole) {
        Role role = new Role(nameRole);
        Role savedRole = roleRepository.save(role);

        // Логирование
        try {
            Map<String, Object> roleData = Map.of(
                    "id", savedRole.getId(),
                    "name", savedRole.getNameRole()
            );

            auditLogService.createAuditLog(
                    currentUserService.getActorForLogging(),
                    "INSERT",
                    "roles",
                    savedRole.getId(),
                    null,
                    roleData
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedRole;
    }

    @Transactional
    public Role save(Role role) {
        boolean isNew = role.getId() == null;
        Map<String, Object> beforeData = null;

        if (!isNew) {
            Optional<Role> existing = findById(role.getId());
            if (existing.isPresent()) {
                beforeData = Map.of(
                    "id", existing.get().getId(),
                    "name", existing.get().getNameRole()
                );
            }
        }

        Role savedRole = roleRepository.save(role);

        // Логирование
        try {
            Map<String, Object> afterData = Map.of(
                    "id", savedRole.getId(),
                    "name", savedRole.getNameRole()
            );

            auditLogService.createAuditLog(
                    currentUserService.getActorForLogging(),
                    isNew ? "INSERT" : "UPDATE",
                    "roles",
                    savedRole.getId(),
                    beforeData,
                    afterData
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return savedRole;
    }

    @Transactional
    public void deleteById(Integer id) {
        Optional<Role> roleOpt = findById(id);
        if (roleOpt.isPresent()) {
            Role role = roleOpt.get();

            // Логирование до удаления
            try {
                Map<String, Object> beforeData = Map.of(
                        "id", role.getId(),
                        "name", role.getNameRole()
                );

                auditLogService.createAuditLog(
                        currentUserService.getActorForLogging(),
                        "DELETE",
                        "roles",
                        id,
                        beforeData,
                        null
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            roleRepository.deleteById(id);
        }
    }

    @Transactional
    public Role update(Integer id, Role role) {
        role.setId(id);
        return save(role);
    }
}