package com.example.server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "divisions")
public class Division {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_division", nullable = false)
    private Integer id;

    @Column(name = "name_division", nullable = false, length = Integer.MAX_VALUE)
    private String nameDivision;

    // Пустой конструктор (нужен для JPA)
    public Division() {
    }

    // Конструктор для создания роли без ID
    public Division(String nameDivision) {
        this.nameDivision = nameDivision;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNameDivision() {
        return nameDivision;
    }

    public void setNameDivision(String nameDivision) {
        this.nameDivision = nameDivision;
    }

}