package com.example.server.service;

import com.example.server.model.AuditLog;
import com.example.server.model.User;
import com.example.server.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public Page<AuditLog> findAll(Pageable pageable) {
        return auditLogRepository.findAllWithUser(pageable);
    }

    public Optional<AuditLog> findById(Long id) {
        return auditLogRepository.findById(id);
    }

    public Page<AuditLog> findByUserId(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable);
    }

    public Page<AuditLog> findByTableName(String tableName, Pageable pageable) {
        return auditLogRepository.findByTableName(tableName, pageable);
    }

    public Page<AuditLog> findByOperationType(String operationType, Pageable pageable) {
        return auditLogRepository.findByOperationType(operationType, pageable);
    }

    public Page<AuditLog> findByOperationAndTable(String operation, String table, Pageable pageable) {
        return auditLogRepository.findByOperationTypeAndTableName(operation, table, pageable);
    }

    public Page<AuditLog> findByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return auditLogRepository.findByChangeTimeBetween(start, end, pageable);
    }

    public Page<AuditLog> searchLogs(String search, String operation, String table, Pageable pageable) {
        if (search == null || search.isEmpty()) {
            return findAll(pageable);
        }
        return auditLogRepository.searchLogs(search.toLowerCase(), operation, table, pageable);
    }

    @Transactional
    public AuditLog save(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    @Transactional
    public void deleteById(Long id) {
        auditLogRepository.deleteById(id);
    }

    @Transactional
    public void deleteOldLogs(LocalDateTime beforeDate) {
        auditLogRepository.deleteByChangeTimeBefore(beforeDate);
    }

    @Transactional
    public AuditLog createAuditLog(User user, String operationType, String tableName,
                                   Integer recordId, Map<String, Object> itemBeforeChange,
                                   Map<String, Object> itemAfterChange) {
        try {
            String beforeJson = itemBeforeChange != null ?
                    objectMapper.writeValueAsString(itemBeforeChange) : null;
            String afterJson = itemAfterChange != null ?
                    objectMapper.writeValueAsString(itemAfterChange) : null;

            AuditLog auditLog = new AuditLog(
                    user,
                    operationType,
                    tableName,
                    recordId,
                    beforeJson,
                    afterJson,
                    LocalDateTime.now()
            );
            return auditLogRepository.save(auditLog);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при создании JSON для лога", e);
        }
    }

    @Transactional
    public AuditLog createAuditLog(User user, String operationType, String tableName,
                                   Integer recordId, String itemBeforeChange,
                                   String itemAfterChange) {
        AuditLog auditLog = new AuditLog(
                user,
                operationType,
                tableName,
                recordId,
                itemBeforeChange,
                itemAfterChange,
                LocalDateTime.now()
        );
        return auditLogRepository.save(auditLog);
    }
}