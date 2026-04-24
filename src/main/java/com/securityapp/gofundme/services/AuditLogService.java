package com.securityapp.gofundme.services;

import com.securityapp.gofundme.model.AuditAction;
import com.securityapp.gofundme.model.AuditLog;
import com.securityapp.gofundme.model.AuditStatus;
import com.securityapp.gofundme.repositories.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    public AuditLogService(AuditLogRepository auditLogRepository) { this.auditLogRepository = auditLogRepository; }

    public void log(AuditAction action, AuditStatus status, String entityName, Long entityId, String description, String oldValue, String newValue, String extraData, HttpServletRequest request) {
        AuditLog log = new AuditLog();
        log.setActorEmail(getCurrentActor());
        log.setAction(action);
        log.setStatus(status);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setDescription(description);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setExtraData(extraData);
        if (request != null) {
            log.setIpAddress(getClientIp(request));
            log.setRequestUri(request.getRequestURI());
            log.setHttpMethod(request.getMethod());
        }
        try { auditLogRepository.save(log); } catch (Exception ignored) { }
    }

    public void log(Authentication authentication, HttpServletRequest request, String action, String entityName, Long entityId, String oldValue, String newValue) {
        AuditLog log = new AuditLog();
        log.setActorEmail(authentication != null ? authentication.getName() : getCurrentActor());
        log.setAction(parseAction(action));
        log.setStatus(AuditStatus.SUCCESS);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setDescription("Action administrative : " + action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        if (request != null) {
            log.setIpAddress(getClientIp(request));
            log.setRequestUri(request.getRequestURI());
            log.setHttpMethod(request.getMethod());
        }
        try { auditLogRepository.save(log); } catch (Exception ignored) { }
    }

    public void log(String actorEmail, String action, String entityName, Long entityId, String oldValue, String newValue, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setActorEmail(actorEmail);
        log.setAction(parseAction(action));
        log.setStatus(AuditStatus.SUCCESS);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setDescription("Action : " + action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setIpAddress(ipAddress);
        try { auditLogRepository.save(log); } catch (Exception ignored) { }
    }

    private String getCurrentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return "SYSTEM";
        return authentication.getName();
    }
    private AuditAction parseAction(String action) {
        if (action == null) return AuditAction.UNKNOWN_ACTION;
        try { return AuditAction.valueOf(action); } catch (Exception e) { return AuditAction.ADMIN_ACTION; }
    }
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
