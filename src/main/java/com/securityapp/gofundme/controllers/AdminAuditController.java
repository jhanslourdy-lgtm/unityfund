package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.repositories.AuditLogRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/audit-logs")
public class AdminAuditController {

    private final AuditLogRepository auditLogRepository;

    public AdminAuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("active", "audit");
        model.addAttribute("logs", auditLogRepository.findTop200ByOrderByCreatedAtDesc());
        return "admin/audit-logs/list";
    }
}
