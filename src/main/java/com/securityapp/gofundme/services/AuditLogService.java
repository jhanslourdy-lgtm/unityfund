package com.securityapp.gofundme.services;

import com.securityapp.gofundme.model.AuditLog;
import com.securityapp.gofundme.repositories.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String actorEmail, String action, String entityName, Long entityId,
                    String oldValue, String newValue, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setActorEmail(actorEmail != null ? actorEmail : "SYSTEM");
        log.setAction(action);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setIpAddress(ipAddress);
        auditLogRepository.save(log);
    }

    public void log(Authentication authentication, HttpServletRequest request, String action,
                    String entityName, Long entityId, String oldValue, String newValue) {
        String actor = authentication != null ? authentication.getName() : "SYSTEM";
        String ip = request != null ? getClientIp(request) : null;
        log(actor, action, entityName, entityId, oldValue, newValue, ip);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
