package com.securityapp.gofundme.services;

import com.securityapp.gofundme.model.AuditAction;
import com.securityapp.gofundme.model.AuditLog;
import com.securityapp.gofundme.model.AuditStatus;
import com.securityapp.gofundme.repositories.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
            AuditAction action,
            AuditStatus status,
            String entityName,
            Long entityId,
            String description,
            String oldValue,
            String newValue,
            String extraData,
            HttpServletRequest request
    ) {
        saveLog(getCurrentActor(), safeName(action), safeName(status), entityName, entityId,
                description, oldValue, newValue, extraData, request);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
            Authentication authentication,
            HttpServletRequest request,
            String action,
            String entityName,
            Long entityId,
            String oldValue,
            String newValue
    ) {
        String actor = authentication != null ? authentication.getName() : getCurrentActor();
        saveLog(actor, normalize(action), "SUCCESS", entityName, entityId,
                "Action administrative : " + normalize(action), oldValue, newValue, null, request);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
            String actorEmail,
            String action,
            String entityName,
            Long entityId,
            String oldValue,
            String newValue,
            String ipAddress
    ) {
        AuditLog audit = new AuditLog();
        audit.setActorEmail(actorEmail == null || actorEmail.isBlank() ? "SYSTEM" : actorEmail);
        audit.setAction(normalize(action));
        audit.setStatus("SUCCESS");
        audit.setEntityName(entityName);
        audit.setEntityId(entityId);
        audit.setDescription("Action : " + normalize(action));
        audit.setOldValue(oldValue);
        audit.setNewValue(newValue);
        audit.setIpAddress(ipAddress);
        save(audit);
    }

    private void saveLog(
            String actor,
            String action,
            String status,
            String entityName,
            Long entityId,
            String description,
            String oldValue,
            String newValue,
            String extraData,
            HttpServletRequest request
    ) {
        AuditLog audit = new AuditLog();
        audit.setActorEmail(actor == null || actor.isBlank() ? "SYSTEM" : actor);
        audit.setAction(action == null || action.isBlank() ? "UNKNOWN_ACTION" : action);
        audit.setStatus(status == null || status.isBlank() ? "SUCCESS" : status);
        audit.setEntityName(entityName);
        audit.setEntityId(entityId);
        audit.setDescription(description);
        audit.setOldValue(oldValue);
        audit.setNewValue(newValue);
        audit.setExtraData(extraData);

        if (request != null) {
            audit.setIpAddress(getClientIp(request));
            audit.setRequestUri(request.getRequestURI());
            audit.setHttpMethod(request.getMethod());
        }

        save(audit);
    }

    private void save(AuditLog audit) {
        try {
            auditLogRepository.save(audit);
        } catch (Exception e) {
            // L'audit ne doit jamais casser une action métier, mais l'erreur doit rester visible dans Render logs.
            log.error("Impossible d'enregistrer l'audit. Vérifie la table audit_logs.", e);
        }
    }

    private String getCurrentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "SYSTEM";
        }
        return authentication.getName();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String safeName(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? "UNKNOWN_ACTION" : value.trim().toUpperCase().replace(' ', '_');
    }
}
