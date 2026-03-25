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

    @Query("SELECT a FROM AuditLog a LEFT JOIN FETCH a.user u ORDER BY a.changeTime DESC")
    Page<AuditLog> findAllWithUser(Pageable pageable);

    @Query("SELECT a FROM AuditLog a LEFT JOIN FETCH a.user u WHERE a.tableName = :tableName ORDER BY a.changeTime DESC")
    Page<AuditLog> findByTableNameWithUser(@Param("tableName") String tableName, Pageable pageable);

    @Query(value = "SELECT a.* FROM audit_log a " +
           "LEFT JOIN users u ON a.id_user = u.id_user " +
           "WHERE (:search IS NULL OR :search = '' OR " +
           "LOWER(a.table_name) LIKE CONCAT('%', :search, '%') OR " +
           "LOWER(a.operation_type) LIKE CONCAT('%', :search, '%') OR " +
           "LOWER(u.login) LIKE CONCAT('%', :search, '%')) AND " +
           "(:operation IS NULL OR :operation = '' OR a.operation_type = :operation) AND " +
           "(:table IS NULL OR :table = '' OR a.table_name = :table) " +
           "ORDER BY a.change_time DESC",
           countQuery = "SELECT COUNT(*) FROM audit_log a " +
           "LEFT JOIN users u ON a.id_user = u.id_user " +
           "WHERE (:search IS NULL OR :search = '' OR " +
           "LOWER(a.table_name) LIKE CONCAT('%', :search, '%') OR " +
           "LOWER(a.operation_type) LIKE CONCAT('%', :search, '%') OR " +
           "LOWER(u.login) LIKE CONCAT('%', :search, '%')) AND " +
           "(:operation IS NULL OR :operation = '' OR a.operation_type = :operation) AND " +
           "(:table IS NULL OR :table = '' OR a.table_name = :table)",
           nativeQuery = true)
    Page<AuditLog> searchLogs(@Param("search") String search,
                              @Param("operation") String operation,
                              @Param("table") String table,
                              Pageable pageable);
}