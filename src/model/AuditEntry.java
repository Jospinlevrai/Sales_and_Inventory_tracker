package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AuditEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private long auditId;
    private Integer userId;
    private String username;
    private String action;
    private String entityType;
    private String entityId;
    private String details;
    private LocalDateTime createdAt;

    public AuditEntry() {
    }

    public AuditEntry(long auditId, Integer userId, String username, String action,
                      String entityType, String entityId, String details, LocalDateTime createdAt) {
        this.auditId = auditId;
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
        this.createdAt = createdAt;
    }

    public long getAuditId() { return auditId; }
    public void setAuditId(long auditId) { this.auditId = auditId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
