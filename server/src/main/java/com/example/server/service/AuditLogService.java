package com.example.server.service;

import com.example.server.model.AuditLog;
import com.example.server.model.User;
import com.example.server.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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

    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }

    public Optional<AuditLog> findById(Long id) {
        return auditLogRepository.findById(id);
    }

    public List<AuditLog> findByUserId(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }

    public List<AuditLog> findByTableName(String tableName) {
        return auditLogRepository.findByTableName(tableName);
    }

    public List<AuditLog> findByOperationType(String operationType) {
        return auditLogRepository.findByOperationType(operationType);
    }

    public List<AuditLog> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByChangeTimeBetween(start, end);
    }

    public List<AuditLog> findByUserAndDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByUserIdAndChangeTimeBetween(userId, start, end);
    }

    public List<AuditLog> findByTableAndRecordId(String tableName, Integer recordId) {
        return auditLogRepository.findByTableNameAndRecordId(tableName, recordId);
    }

    public List<AuditLog> findLatest(int limit) {
        return auditLogRepository.findLatestWithUser(limit);
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
        List<AuditLog> oldLogs = auditLogRepository.findByChangeTimeBetween(
                LocalDateTime.of(1970, 1, 1, 0, 0), beforeDate
        );
        auditLogRepository.deleteAll(oldLogs);
    }

    /**
     * Метод для создания записи аудита
     */
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

    /**
     * Перегруженный метод для работы со строками JSON (если JSON уже готов)
     */
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