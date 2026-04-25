package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.repositories.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminAuditController {

    private static final Logger log = LoggerFactory.getLogger(AdminAuditController.class);
    private final AuditLogRepository auditLogRepository;

    public AdminAuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/admin/audit-logs")
    public String auditLogs(Model model) {
        try {
            model.addAttribute("logs", auditLogRepository.findTop200ByOrderByCreatedAtDesc());
            model.addAttribute("auditError", null);
        } catch (Exception e) {
            log.error("Erreur pendant le chargement des audit logs", e);
            model.addAttribute("logs", java.util.Collections.emptyList());
            model.addAttribute("auditError", "Impossible de charger les audits. Vérifie la structure MySQL de audit_logs dans la base utilisée par Render.");
        }
        return "admin/audit-logs/list";
    }
}
