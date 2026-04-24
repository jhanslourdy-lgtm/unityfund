package com.securityapp.gofundme.config;

import com.securityapp.gofundme.model.AuditAction;
import com.securityapp.gofundme.model.AuditStatus;
import com.securityapp.gofundme.services.AuditLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Set;

@Component
public class UserActivityAuditFilter extends OncePerRequestFilter {

    private final AuditLogService auditLogService;
    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    public UserActivityAuditFilter(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !MUTATING_METHODS.contains(request.getMethod())
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.startsWith("/uploads/")
                || uri.startsWith("/admin/audit-logs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        Exception thrown = null;
        try {
            filterChain.doFilter(request, responseWrapper);
        } catch (Exception e) {
            thrown = e;
            throw e;
        } finally {
            int status = responseWrapper.getStatus();
            AuditStatus auditStatus = thrown != null || status >= 400 ? AuditStatus.FAILED : AuditStatus.SUCCESS;
            AuditAction action = auditStatus == AuditStatus.SUCCESS ? AuditAction.USER_ACTION_SUCCESS : AuditAction.USER_ACTION_FAILED;
            String summary = request.getMethod() + " " + request.getRequestURI() + " => HTTP " + status;
            auditLogService.log(action, auditStatus, "HTTP_REQUEST", null, summary, null, null,
                    thrown != null ? thrown.getMessage() : null, request);
            responseWrapper.copyBodyToResponse();
        }
    }
}
