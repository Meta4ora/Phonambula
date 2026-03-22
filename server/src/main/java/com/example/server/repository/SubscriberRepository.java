package com.example.server.repository;

import com.example.server.dto.ContactDTO;
import com.example.server.model.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

    // ================= БАЗОВЫЕ МЕТОДЫ =================

    Optional<Subscriber> findById(Long id);

    // ================= МЕТОДЫ С FETCH (из первого файла) =================

    @Query("SELECT s FROM Subscriber s " +
            "LEFT JOIN FETCH s.idPost " +
            "LEFT JOIN FETCH s.idDivision " +
            "LEFT JOIN FETCH s.idBuilding")
    List<Subscriber> findAllWithRelations();

    // ================= ПОИСК ПО ID (из обоих файлов) =================

    List<Subscriber> findByIdBuildingId(Integer buildingId);
    List<Subscriber> findByIdDivisionId(Integer divisionId);
    List<Subscriber> findByIdPostId(Integer postId);
    List<Subscriber> findByIdUserId(Long userId);

    // ================= ПОИСК ПО ОЧИЩЕННЫМ НОМЕРАМ (объединенный) =================

    // Из первого файла - с использованием REGEXP_REPLACE
    @Query("SELECT s FROM Subscriber s WHERE " +
            "REGEXP_REPLACE(s.mobilePhoneNumber, '[^0-9]', '', 'g') = :cleanNumber")
    List<Subscriber> findByCleanMobilePhoneNumber(@Param("cleanNumber") String cleanNumber);

    @Query("SELECT s FROM Subscriber s WHERE " +
            "REGEXP_REPLACE(s.landlinePhoneNumber, '[^0-9]', '', 'g') = :cleanNumber")
    List<Subscriber> findByCleanLandlinePhoneNumber(@Param("cleanNumber") String cleanNumber);

    @Query("SELECT s FROM Subscriber s WHERE " +
            "REGEXP_REPLACE(s.internalPhoneNumber, '[^0-9]', '', 'g') = :cleanNumber")
    List<Subscriber> findByCleanInternalPhoneNumber(@Param("cleanNumber") String cleanNumber);

    // ================= ПОЛНОТЕКСТОВЫЙ ПОИСК ДЛЯ SUBSCRIBER (из первого файла) =================

    @Query("SELECT s FROM Subscriber s WHERE " +
            "LOWER(s.idUser.surname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.idUser.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.idUser.patronymic) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.idPost.namePost) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.idDivision.nameDivision) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.idBuilding.nameBuilding) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.cabinetNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.internalPhoneNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.landlinePhoneNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.mobilePhoneNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Subscriber> searchSubscribers(@Param("searchTerm") String searchTerm);

    // ================= МЕТОДЫ ДЛЯ CONTACTDTO (из второго файла) =================

    // Все контакты
    @Query("SELECT new com.example.server.dto.ContactDTO(" +
            "s.id, " +
            "u.surname, u.name, u.patronymic, " +
            "p.namePost, " +
            "d.nameDivision, " +
            "b.nameBuilding, " +
            "s.cabinetNumber, " +
            "s.internalPhoneNumber, " +
            "s.landlinePhoneNumber, " +
            "s.mobilePhoneNumber) " +
            "FROM Subscriber s " +
            "JOIN s.idUser u " +
            "JOIN s.idPost p " +
            "JOIN s.idDivision d " +
            "JOIN s.idBuilding b " +
            "ORDER BY u.surname, u.name")
    List<ContactDTO> findAllContacts();

    // УНИВЕРСАЛЬНЫЙ ПОИСК ПО ВСЕМ ПОЛЯМ (для ContactDTO)
    @Query("SELECT new com.example.server.dto.ContactDTO(" +
            "s.id, " +
            "u.surname, u.name, u.patronymic, " +
            "p.namePost, " +
            "d.nameDivision, " +
            "b.nameBuilding, " +
            "s.cabinetNumber, " +
            "s.internalPhoneNumber, " +
            "s.landlinePhoneNumber, " +
            "s.mobilePhoneNumber) " +
            "FROM Subscriber s " +
            "JOIN s.idUser u " +
            "JOIN s.idPost p " +
            "JOIN s.idDivision d " +
            "JOIN s.idBuilding b " +
            "WHERE " +
            "LOWER(u.surname) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.patronymic) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.namePost) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.nameDivision) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.nameBuilding) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.cabinetNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "s.mobilePhoneNumber LIKE CONCAT('%', :search, '%') OR " +
            "s.landlinePhoneNumber LIKE CONCAT('%', :search, '%') OR " +
            "s.internalPhoneNumber LIKE CONCAT('%', :search, '%') " +
            "ORDER BY u.surname, u.name")
    List<ContactDTO> searchAllContacts(@Param("search") String search);

    // ПОИСК ПО ТЕЛЕФОНУ (для ContactDTO)
    @Query("SELECT new com.example.server.dto.ContactDTO(" +
            "s.id, " +
            "u.surname, u.name, u.patronymic, " +
            "p.namePost, " +
            "d.nameDivision, " +
            "b.nameBuilding, " +
            "s.cabinetNumber, " +
            "s.internalPhoneNumber, " +
            "s.landlinePhoneNumber, " +
            "s.mobilePhoneNumber) " +
            "FROM Subscriber s " +
            "JOIN s.idUser u " +
            "JOIN s.idPost p " +
            "JOIN s.idDivision d " +
            "JOIN s.idBuilding b " +
            "WHERE " +
            "s.mobilePhoneNumber LIKE CONCAT('%', :phone, '%') OR " +
            "s.landlinePhoneNumber LIKE CONCAT('%', :phone, '%') OR " +
            "s.internalPhoneNumber LIKE CONCAT('%', :phone, '%') " +
            "ORDER BY u.surname, u.name")
    List<ContactDTO> searchContactsByPhone(@Param("phone") String phone);

    // Фильтр по отделу
    @Query("SELECT new com.example.server.dto.ContactDTO(" +
            "s.id, " +
            "u.surname, u.name, u.patronymic, " +
            "p.namePost, " +
            "d.nameDivision, " +
            "b.nameBuilding, " +
            "s.cabinetNumber, " +
            "s.internalPhoneNumber, " +
            "s.landlinePhoneNumber, " +
            "s.mobilePhoneNumber) " +
            "FROM Subscriber s " +
            "JOIN s.idUser u " +
            "JOIN s.idPost p " +
            "JOIN s.idDivision d " +
            "JOIN s.idBuilding b " +
            "WHERE d.id = :divisionId " +
            "ORDER BY u.surname, u.name")
    List<ContactDTO> findContactsByDivisionId(@Param("divisionId") Integer divisionId);

    // Фильтр по зданию
    @Query("SELECT new com.example.server.dto.ContactDTO(" +
            "s.id, " +
            "u.surname, u.name, u.patronymic, " +
            "p.namePost, " +
            "d.nameDivision, " +
            "b.nameBuilding, " +
            "s.cabinetNumber, " +
            "s.internalPhoneNumber, " +
            "s.landlinePhoneNumber, " +
            "s.mobilePhoneNumber) " +
            "FROM Subscriber s " +
            "JOIN s.idUser u " +
            "JOIN s.idPost p " +
            "JOIN s.idDivision d " +
            "JOIN s.idBuilding b " +
            "WHERE b.id = :buildingId " +
            "ORDER BY u.surname, u.name")
    List<ContactDTO> findContactsByBuildingId(@Param("buildingId") Integer buildingId);

    // Фильтр по должности
    @Query("SELECT new com.example.server.dto.ContactDTO(" +
            "s.id, " +
            "u.surname, u.name, u.patronymic, " +
            "p.namePost, " +
            "d.nameDivision, " +
            "b.nameBuilding, " +
            "s.cabinetNumber, " +
            "s.internalPhoneNumber, " +
            "s.landlinePhoneNumber, " +
            "s.mobilePhoneNumber) " +
            "FROM Subscriber s " +
            "JOIN s.idUser u " +
            "JOIN s.idPost p " +
            "JOIN s.idDivision d " +
            "JOIN s.idBuilding b " +
            "WHERE p.id = :postId " +
            "ORDER BY u.surname, u.name")
    List<ContactDTO> findContactsByPostId(@Param("postId") Integer postId);

    // ПОИСК ПО НОМЕРУ ТЕЛЕФОНА (частичное совпадение)
    @Query("SELECT DISTINCT s FROM Subscriber s " +
            "WHERE " +
            "s.mobilePhoneNumber LIKE CONCAT('%', :phone, '%') OR " +
            "s.landlinePhoneNumber LIKE CONCAT('%', :phone, '%') OR " +
            "s.internalPhoneNumber LIKE CONCAT('%', :phone, '%')")
    List<Subscriber> searchByPhone(@Param("phone") String phone);
}