package com.example.server.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "divisions")
public class Division {
    @Id
    @Column(name = "id_division", nullable = false)
    private Integer id;

    @Column(name = "name_division", nullable = false, length = Integer.MAX_VALUE)
    private String nameDivision;

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