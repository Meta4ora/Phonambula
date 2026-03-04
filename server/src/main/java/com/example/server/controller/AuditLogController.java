package com.example.server.controller;

import com.example.server.model.AuditLog;
import com.example.server.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @Autowired
    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Получить все записи аудита
     */
    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        List<AuditLog> logs = auditLogService.findAll();
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    /**
     * Получить запись аудита по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuditLog> getAuditLogById(@PathVariable Long id) {
        return auditLogService.findById(id)
                .map(log -> new ResponseEntity<>(log, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Получить записи аудита по пользователю
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByUserId(@PathVariable Long userId) {
        List<AuditLog> logs = auditLogService.findByUserId(userId);
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    /**
     * Получить записи аудита по таблице
     */
    @GetMapping("/table/{tableName}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByTableName(@PathVariable String tableName) {
        List<AuditLog> logs = auditLogService.findByTableName(tableName);
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    /**
     * Получить записи аудита по типу операции
     */
    @GetMapping("/operation/{operationType}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByOperationType(@PathVariable String operationType) {
        List<AuditLog> logs = auditLogService.findByOperationType(operationType);
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    /**
     * Получить записи аудита за период
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<AuditLog>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<AuditLog> logs = auditLogService.findByDateRange(start, end);
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    /**
     * Получить записи аудита по таблице и ID записи
     */
    @GetMapping("/record")
    public ResponseEntity<List<AuditLog>> getAuditLogsByTableAndRecordId(
            @RequestParam String tableName,
            @RequestParam Integer recordId) {
        List<AuditLog> logs = auditLogService.findByTableAndRecordId(tableName, recordId);
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    /**
     * Получить последние сколько-то записей аудита
     */
    @GetMapping("/latest")
    public ResponseEntity<List<AuditLog>> getLatestAuditLogs(
            @RequestParam(defaultValue = "100") int limit) {
        List<AuditLog> logs = auditLogService.findLatest(limit);
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    /**
     * Удалить запись аудита (только для админов)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuditLog(@PathVariable Long id) {
        if (!auditLogService.findById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        auditLogService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Удалить старые записи аудита (только для админов)
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> cleanupOldLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeDate) {
        auditLogService.deleteOldLogs(beforeDate);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}