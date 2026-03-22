package com.example.server.dto;

public class ContactDTO {
    private Long id;
    private String fullName;
    private String post;
    private String division;
    private String building;
    private String cabinetNumber;
    private String internalPhone;
    private String landlinePhone;
    private String mobilePhone;

    public ContactDTO(Long id, String userSurname, String userName, String userPatronymic,
                      String postName, String divisionName, String buildingName,
                      String cabinetNumber, String internalPhone,
                      String landlinePhone, String mobilePhone) {
        this.id = id;
        this.fullName = buildFullName(userSurname, userName, userPatronymic);
        this.post = postName;
        this.division = divisionName;
        this.building = buildingName;
        this.cabinetNumber = cabinetNumber;
        this.internalPhone = internalPhone;
        this.landlinePhone = landlinePhone;
        this.mobilePhone = mobilePhone;
    }

    private String buildFullName(String surname, String name, String patronymic) {
        StringBuilder sb = new StringBuilder();
        if (surname != null) sb.append(surname).append(" ");
        if (name != null) sb.append(name).append(" ");
        if (patronymic != null) sb.append(patronymic);
        return sb.toString().trim();
    }

    // Геттеры
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPost() { return post; }
    public String getDivision() { return division; }
    public String getBuilding() { return building; }
    public String getCabinetNumber() { return cabinetNumber; }
    public String getInternalPhone() { return internalPhone; }
    public String getLandlinePhone() { return landlinePhone; }
    public String getMobilePhone() { return mobilePhone; }
}