package com.example.server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role", nullable = false)
    private Integer id;

    @Column(name = "name_role", nullable = false, length = Integer.MAX_VALUE)
    private String nameRole;

    // Пустой конструктор (нужен для JPA)
    public Role() {
    }

    // Конструктор для создания роли без ID
    public Role(String nameRole) {
        this.nameRole = nameRole;
    }

    // Геттеры и сеттеры
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNameRole() {
        return nameRole;
    }

    public void setNameRole(String nameRole) {
        this.nameRole = nameRole;
    }
}