package com.example.server.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_user")
    private User user;

    @Column(name = "operation_type", nullable = false, length = 10)
    private String operationType;

    @Column(name = "table_name", nullable = false, length = 50)
    private String tableName;

    @Column(name = "record_id", nullable = false)
    private Integer recordId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "item_before_change", columnDefinition = "jsonb")
    private String itemBeforeChange;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "item_after_change", columnDefinition = "jsonb")
    private String itemAfterChange;

    @Column(name = "change_time", nullable = false)
    private LocalDateTime changeTime;

    public AuditLog() {
    }

    // Конструктор для создания записи аудита
    public AuditLog(User user, String operationType, String tableName,
                    Integer recordId, String itemBeforeChange,
                    String itemAfterChange, LocalDateTime changeTime) {
        this.user = user;
        this.operationType = operationType;
        this.tableName = tableName;
        this.recordId = recordId;
        this.itemBeforeChange = itemBeforeChange;
        this.itemAfterChange = itemAfterChange;
        this.changeTime = changeTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public String getItemBeforeChange() {
        return itemBeforeChange;
    }

    public void setItemBeforeChange(String itemBeforeChange) {
        this.itemBeforeChange = itemBeforeChange;
    }

    public String getItemAfterChange() {
        return itemAfterChange;
    }

    public void setItemAfterChange(String itemAfterChange) {
        this.itemAfterChange = itemAfterChange;
    }

    public LocalDateTime getChangeTime() {
        return changeTime;
    }

    public void setChangeTime(LocalDateTime changeTime) {
        this.changeTime = changeTime;
    }
}