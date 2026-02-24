package com.example.server.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "subscribers")
public class Subscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_subscriber", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User idUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_post", nullable = false)
    private Post idPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_division", nullable = false)
    private Division idDivision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_building", nullable = false)
    private Building idBuilding;

    @Column(name = "date_birth")
    private LocalDate dateBirth;

    @Column(name = "cabinet_number", length = Integer.MAX_VALUE)
    private String cabinetNumber;

    @Column(name = "internal_phone_number", length = Integer.MAX_VALUE)
    private String internalPhoneNumber;

    @Column(name = "landline_phone_number", length = Integer.MAX_VALUE)
    private String landlinePhoneNumber;

    @Column(name = "mobile_phone_number", length = Integer.MAX_VALUE)
    private String mobilePhoneNumber;

    // Пустой конструктор
    public Subscriber() {
    }

    // Конструктор для создания абонента
    public Subscriber(User user, Post post, Division division, Building building,
                      LocalDate dateBirth, String cabinetNumber, String internalPhoneNumber,
                      String landlinePhoneNumber, String mobilePhoneNumber) {
        this.idUser = user;
        this.idPost = post;
        this.idDivision = division;
        this.idBuilding = building;
        this.dateBirth = dateBirth;
        this.cabinetNumber = cabinetNumber;
        this.internalPhoneNumber = internalPhoneNumber;
        this.landlinePhoneNumber = landlinePhoneNumber;
        this.mobilePhoneNumber = mobilePhoneNumber;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getIdUser() {
        return idUser;
    }

    public void setIdUser(User idUser) {
        this.idUser = idUser;
    }

    public Post getIdPost() {
        return idPost;
    }

    public void setIdPost(Post idPost) {
        this.idPost = idPost;
    }

    public Division getIdDivision() {
        return idDivision;
    }

    public void setIdDivision(Division idDivision) {
        this.idDivision = idDivision;
    }

    public Building getIdBuilding() {
        return idBuilding;
    }

    public void setIdBuilding(Building idBuilding) {
        this.idBuilding = idBuilding;
    }

    public LocalDate getDateBirth() {
        return dateBirth;
    }

    public void setDateBirth(LocalDate dateBirth) {
        this.dateBirth = dateBirth;
    }

    public String getCabinetNumber() {
        return cabinetNumber;
    }

    public void setCabinetNumber(String cabinetNumber) {
        this.cabinetNumber = cabinetNumber;
    }

    public String getInternalPhoneNumber() {
        return internalPhoneNumber;
    }

    public void setInternalPhoneNumber(String internalPhoneNumber) {
        this.internalPhoneNumber = internalPhoneNumber;
    }

    public String getLandlinePhoneNumber() {
        return landlinePhoneNumber;
    }

    public void setLandlinePhoneNumber(String landlinePhoneNumber) {
        this.landlinePhoneNumber = landlinePhoneNumber;
    }

    public String getMobilePhoneNumber() {
        return mobilePhoneNumber;
    }

    public void setMobilePhoneNumber(String mobilePhoneNumber) {
        this.mobilePhoneNumber = mobilePhoneNumber;
    }
}