package com.example.server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "buildings")
public class Building {
    @Id
    @Column(name = "id_building", nullable = false)
    private Integer id;

    @Column(name = "name_building", nullable = false, length = Integer.MAX_VALUE)
    private String nameBuilding;

    @Column(name = "address", nullable = false, length = Integer.MAX_VALUE)
    private String address;

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