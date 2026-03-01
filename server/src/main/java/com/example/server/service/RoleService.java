package com.example.server.service;

import com.example.server.model.Role;
import com.example.server.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Optional<Role> findById(Integer id) {
        return roleRepository.findById(id);
    }

    @Transactional
    public Role createRole(String nameRole) {
        // Используем конструктор
        Role role = new Role(nameRole);
        return roleRepository.save(role);
    }

    @Transactional
    public Role save(Role role) {
        return roleRepository.save(role);
    }

    @Transactional
    public void deleteById(Integer id) {
        roleRepository.deleteById(id);
    }

    @Transactional
    public Role update(Integer id, Role role) {
        role.setId(id);
        return roleRepository.save(role);
    }
}