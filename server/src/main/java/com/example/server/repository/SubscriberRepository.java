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

    @Query("SELECT s FROM Subscriber s WHERE " +
            "LOWER(s.idUser.surname) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.idUser.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.idUser.patronymic) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.idPost.namePost) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.idDivision.nameDivision) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.idBuilding.nameBuilding) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Subscriber> searchSubscribers(@Param("searchTerm") String searchTerm);
}