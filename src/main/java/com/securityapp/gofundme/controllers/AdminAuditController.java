package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.repositories.AuditLogRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminAuditController {

    private final AuditLogRepository auditLogRepository;

    public AdminAuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/admin/audit-logs")
    public String auditLogs(Model model) {
        model.addAttribute("logs", auditLogRepository.findTop200ByOrderByCreatedAtDesc());
        return "admin/audit-logs/list";
    }
}