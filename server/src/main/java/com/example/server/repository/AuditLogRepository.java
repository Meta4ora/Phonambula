package com.example.server.repository;

import com.example.server.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByUserId(Long userId, Pageable pageable);
    
    Page<AuditLog> findByTableName(String tableName, Pageable pageable);
    
    Page<AuditLog> findByOperationType(String operationType, Pageable pageable);
    
    Page<AuditLog> findByChangeTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    Page<AuditLog> findByOperationTypeAndTableName(String operationType, String tableName, Pageable pageable);
    
    void deleteByChangeTimeBefore(LocalDateTime dateTime);

    @Query("SELECT a FROM AuditLog a LEFT JOIN FETCH a.user u WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(a.tableName) LIKE CONCAT('%', LOWER(:search), '%') OR " +
           "LOWER(a.operationType) LIKE CONCAT('%', LOWER(:search), '%') OR " +
           "LOWER(a.user.login) LIKE CONCAT('%', LOWER(:search), '%')) AND " +
           "(:operation IS NULL OR :operation = '' OR a.operationType = :operation) AND " +
           "(:table IS NULL OR :table = '' OR a.tableName = :table) " +
           "ORDER BY a.changeTime DESC")
    Page<AuditLog> searchLogs(@Param("search") String search,
                              @Param("operation") String operation,
                              @Param("table") String table,
                              Pageable pageable);
}