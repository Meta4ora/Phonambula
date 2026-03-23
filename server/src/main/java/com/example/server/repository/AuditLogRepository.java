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

    List<AuditLog> findByUserId(Long userId);

    List<AuditLog> findByTableName(String tableName);

    List<AuditLog> findByOperationType(String operationType);

    List<AuditLog> findByChangeTimeBetween(LocalDateTime start, LocalDateTime end);

    List<AuditLog> findByUserIdAndChangeTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);

    List<AuditLog> findByTableNameAndRecordId(String tableName, Integer recordId);

    @Query("SELECT a FROM AuditLog a JOIN FETCH a.user u ORDER BY a.changeTime DESC")
    List<AuditLog> findAllWithUser();

    @Query("SELECT a FROM AuditLog a JOIN FETCH a.user u WHERE a.tableName = :tableName ORDER BY a.changeTime DESC")
    List<AuditLog> findByTableNameWithUser(@Param("tableName") String tableName);

    @Query("SELECT a FROM AuditLog a JOIN FETCH a.user u ORDER BY a.changeTime DESC LIMIT :limit")
    List<AuditLog> findLatestWithUser(@Param("limit") int limit);
}