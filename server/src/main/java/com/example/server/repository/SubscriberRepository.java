package com.example.server.repository;

import com.example.server.model.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

    List<Subscriber> findByIdBuildingId(Integer buildingId);

    List<Subscriber> findByIdDivisionId(Integer divisionId);

    List<Subscriber> findByIdPostId(Integer postId);

    List<Subscriber> findByIdUserId(Long userId);

    // Поиск по очищенным номерам телефонов
    @Query("SELECT s FROM Subscriber s WHERE " +
            "REGEXP_REPLACE(s.mobilePhoneNumber, '[^0-9]', '', 'g') = :cleanNumber")
    List<Subscriber> findByCleanMobilePhoneNumber(@Param("cleanNumber") String cleanNumber);

    @Query("SELECT s FROM Subscriber s WHERE " +
            "REGEXP_REPLACE(s.landlinePhoneNumber, '[^0-9]', '', 'g') = :cleanNumber")
    List<Subscriber> findByCleanLandlinePhoneNumber(@Param("cleanNumber") String cleanNumber);

    @Query("SELECT s FROM Subscriber s WHERE " +
            "REGEXP_REPLACE(s.internalPhoneNumber, '[^0-9]', '', 'g') = :cleanNumber")
    List<Subscriber> findByCleanInternalPhoneNumber(@Param("cleanNumber") String cleanNumber);

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
}