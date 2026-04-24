package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.repositories.AuditLogRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;

@Controller
public class AdminAuditController {
    private final AuditLogRepository auditLogRepository;

    public AdminAuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/admin/audit-logs")
    public String list(Model model) {
        try {
            model.addAttribute("logs", auditLogRepository.findTop200ByOrderByCreatedAtDesc());
        } catch (Exception e) {
            model.addAttribute("logs", Collections.emptyList());
            model.addAttribute("auditError", "Impossible de charger les audits. Vérifie que la table audit_logs existe et que ses colonnes correspondent à l'entité AuditLog.");
        }
        return "admin/audit-logs/list";
    }
}
