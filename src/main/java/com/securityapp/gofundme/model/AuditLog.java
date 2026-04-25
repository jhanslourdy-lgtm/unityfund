package com.securityapp.gofundme.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_email", length = 255)
    private String actorEmail;

    @Column(name = "action", length = 120)
    private String action;

    @Column(name = "status", length = 60)
    private String status;

    @Column(name = "entity_name", length = 255)
    private String entityName;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "extra_data", columnDefinition = "TEXT")
    private String extraData;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "request_uri", length = 500)
    private String requestUri;

    @Column(name = "http_method", length = 20)
    private String httpMethod;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }

    public String getActorEmail() { return actorEmail; }
    public void setActorEmail(String actorEmail) { this.actorEmail = actorEmail; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public void setAction(AuditAction action) { this.action = action == null ? null : action.name(); }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public void setStatus(AuditStatus status) { this.status = status == null ? null : status.name(); }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getExtraData() { return extraData; }
    public void setExtraData(String extraData) { this.extraData = extraData; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getRequestUri() { return requestUri; }
    public void setRequestUri(String requestUri) { this.requestUri = requestUri; }

    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
