package com.securityapp.gofundme.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String actorEmail;
    @Enumerated(EnumType.STRING)
    @Column(length = 80)
    private AuditAction action;
    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private AuditStatus status;
    private String entityName;
    private Long entityId;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(columnDefinition = "TEXT")
    private String oldValue;
    @Column(columnDefinition = "TEXT")
    private String newValue;
    @Column(columnDefinition = "TEXT")
    private String extraData;
    private String ipAddress;
    private String requestUri;
    private String httpMethod;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public String getActorEmail() { return actorEmail; }
    public void setActorEmail(String actorEmail) { this.actorEmail = actorEmail; }
    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }
    public AuditStatus getStatus() { return status; }
    public void setStatus(AuditStatus status) { this.status = status; }
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
