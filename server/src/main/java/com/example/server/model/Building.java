package com.example.server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "buildings")
public class Building {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_building", nullable = false)
    private Integer id;

    @Column(name = "name_building", nullable = false, length = Integer.MAX_VALUE)
    private String nameBuilding;

    @Column(name = "address", nullable = false, length = Integer.MAX_VALUE)
    private String address;

    // Пустой конструктор (нужен для JPA)
    public Building() {
    }

    // Конструктор для создания роли без ID
    public Building(String nameBuilding, String address) {
        this.nameBuilding = nameBuilding;
        this.address = address;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNameBuilding() {
        return nameBuilding;
    }

    public void setNameBuilding(String nameBuilding) {
        this.nameBuilding = nameBuilding;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}