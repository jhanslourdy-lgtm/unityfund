package com.securityapp.gofundme.services;

import com.securityapp.gofundme.model.*;
import com.securityapp.gofundme.repositories.AuditLogRepository;
import com.securityapp.gofundme.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    public void log(AuditAction action, AuditStatus status, String entityName, Long entityId,
                    String summary, String oldValue, String newValue, String errorMessage,
                    HttpServletRequest request) {
        try {
            AuditLog log = new AuditLog();
            fillActor(log);
            log.setAction(action);
            log.setStatus(status);
            log.setEntityName(entityName);
            log.setEntityId(entityId);
            log.setSummary(limit(summary, 500));
            log.setOldValue(oldValue);
            log.setNewValue(newValue);
            log.setErrorMessage(limit(errorMessage, 4000));
            fillRequest(log, request);
            auditLogRepository.save(log);
        } catch (Exception ignored) {
            // L'audit ne doit jamais casser l'action principale de l'application.
        }
    }

    public void attempt(AuditAction action, String entityName, Long entityId, String summary, String newValue, HttpServletRequest request) {
        log(action, AuditStatus.ATTEMPT, entityName, entityId, summary, null, newValue, null, request);
    }

    public void success(AuditAction action, String entityName, Long entityId, String summary, String oldValue, String newValue, HttpServletRequest request) {
        log(action, AuditStatus.SUCCESS, entityName, entityId, summary, oldValue, newValue, null, request);
    }

    public void failed(AuditAction action, String entityName, Long entityId, String summary, String newValue, Exception e, HttpServletRequest request) {
        log(action, AuditStatus.FAILED, entityName, entityId, summary, null, newValue, e != null ? e.getMessage() : null, request);
    }

    public void removed(AuditAction action, String entityName, Long entityId, String summary, String oldValue, HttpServletRequest request) {
        log(action, AuditStatus.REMOVED, entityName, entityId, summary, oldValue, null, null, request);
    }

    public void system(AuditAction action, AuditStatus status, String entityName, Long entityId, String summary, String oldValue, String newValue, String errorMessage) {
        try {
            AuditLog log = new AuditLog();
            log.setActorEmail("SYSTEM");
            log.setAction(action);
            log.setStatus(status);
            log.setEntityName(entityName);
            log.setEntityId(entityId);
            log.setSummary(limit(summary, 500));
            log.setOldValue(oldValue);
            log.setNewValue(newValue);
            log.setErrorMessage(limit(errorMessage, 4000));
            auditLogRepository.save(log);
        } catch (Exception ignored) {}
    }

    private void fillActor(AuditLog log) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            log.setActorEmail("ANONYMOUS");
            return;
        }
        String email = authentication.getName();
        log.setActorEmail(email);
        userRepository.findByEmail(email).ifPresent(user -> log.setActorId(user.getId()));
    }

    private void fillRequest(AuditLog log, HttpServletRequest request) {
        if (request == null) return;
        log.setIpAddress(getClientIp(request));
        log.setHttpMethod(request.getMethod());
        String query = request.getQueryString();
        log.setRequestUri(limit(request.getRequestURI() + (query != null ? "?" + query : ""), 1000));
        log.setUserAgent(limit(request.getHeader("User-Agent"), 1000));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) return forwardedFor.split(",")[0].trim();
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) return realIp;
        return request.getRemoteAddr();
    }

    private String limit(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }
}
