package com.example.server.repository;

import com.example.server.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Поиск по пользователю
    List<AuditLog> findByUserId(Long userId);

    // Поиск по таблице
    List<AuditLog> findByTableName(String tableName);

    // Поиск по типу операции
    List<AuditLog> findByOperationType(String operationType);

    // Поиск по временному промежутку
    List<AuditLog> findByChangeTimeBetween(LocalDateTime start, LocalDateTime end);

    // Поиск по пользователю и временному промежутку
    List<AuditLog> findByUserIdAndChangeTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);

    // Поиск по таблице и ID записи
    List<AuditLog> findByTableNameAndRecordId(String tableName, Integer recordId);

    // запрос с JOIN для получения данных пользователя
    @Query("SELECT a FROM AuditLog a JOIN FETCH a.user u WHERE a.tableName = :tableName ORDER BY a.changeTime DESC")
    List<AuditLog> findByTableNameWithUser(@Param("tableName") String tableName);

    // Последние N записей
    @Query("SELECT a FROM AuditLog a JOIN FETCH a.user ORDER BY a.changeTime DESC LIMIT :limit")
    List<AuditLog> findLatestWithUser(@Param("limit") int limit);
}