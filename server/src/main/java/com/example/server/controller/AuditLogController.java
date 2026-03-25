package com.example.server.controller;

import com.example.server.model.AuditLog;
import com.example.server.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String table) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("changeTime").descending());
        Page<AuditLog> auditPage;
        
        // Используем разные методы в зависимости от фильтров
        if (search != null && !search.isEmpty()) {
            auditPage = auditLogService.searchLogs(search, operation, table, pageable);
        } else if (operation != null && !operation.isEmpty() && table != null && !table.isEmpty()) {
            auditPage = auditLogService.findByOperationAndTable(operation, table, pageable);
        } else if (operation != null && !operation.isEmpty()) {
            auditPage = auditLogService.findByOperationType(operation, pageable);
        } else if (table != null && !table.isEmpty()) {
            auditPage = auditLogService.findByTableName(table, pageable);
        } else {
            auditPage = auditLogService.findAll(pageable);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", auditPage.getContent());
        response.put("totalPages", auditPage.getTotalPages());
        response.put("totalElements", auditPage.getTotalElements());
        response.put("currentPage", auditPage.getNumber());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLog> getAuditLogById(@PathVariable Long id) {
        return auditLogService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("changeTime").descending());
        Page<AuditLog> logs = auditLogService.findByUserId(userId, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/table/{tableName}")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByTableName(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("changeTime").descending());
        Page<AuditLog> logs = auditLogService.findByTableName(tableName, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/operation/{operationType}")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByOperationType(
            @PathVariable String operationType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("changeTime").descending());
        Page<AuditLog> logs = auditLogService.findByOperationType(operationType, pageable);
        return ResponseEntity.ok(logs);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuditLog(@PathVariable Long id) {
        if (!auditLogService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        auditLogService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}