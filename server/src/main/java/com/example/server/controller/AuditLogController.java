package com.example.server.controller;

import com.example.server.model.AuditLog;
import com.example.server.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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
        
        List<AuditLog> logs;
        
        if (search != null && !search.isEmpty()) {
            logs = auditLogService.searchLogs(search, operation, table);
        } else if (operation != null && !operation.isEmpty()) {
            logs = auditLogService.findByOperationType(operation);
        } else if (table != null && !table.isEmpty()) {
            logs = auditLogService.findByTableName(table);
        } else {
            logs = auditLogService.findAll();
        }
        
        // Пагинация
        Pageable pageable = PageRequest.of(page, size, Sort.by("changeTime").descending());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), logs.size());
        
        List<AuditLog> pageContent = start < logs.size() ? logs.subList(start, end) : List.of();
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", pageContent);
        response.put("totalPages", (int) Math.ceil((double) logs.size() / size));
        response.put("totalElements", logs.size());
        response.put("currentPage", page);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLog> getAuditLogById(@PathVariable Long id) {
        return auditLogService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByUserId(@PathVariable Long userId) {
        List<AuditLog> logs = auditLogService.findByUserId(userId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/table/{tableName}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByTableName(@PathVariable String tableName) {
        List<AuditLog> logs = auditLogService.findByTableName(tableName);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/operation/{operationType}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByOperationType(@PathVariable String operationType) {
        List<AuditLog> logs = auditLogService.findByOperationType(operationType);
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