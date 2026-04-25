package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.AuditAction;
import com.securityapp.gofundme.model.AuditStatus;
import com.securityapp.gofundme.model.Report;
import com.securityapp.gofundme.repositories.ReportRepository;
import com.securityapp.gofundme.services.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {
    private final ReportRepository reportRepository;
    private final AuditLogService auditLogService;

    public AdminReportController(ReportRepository reportRepository, AuditLogService auditLogService) {
        this.reportRepository = reportRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String list(Model model) {
        model.addAttribute("reports", reportRepository.findAll());
        return "admin/reports/list";
    }

    @PostMapping("/{id}/status")
    public String changeStatusPost(@PathVariable Long id, @RequestParam Report.ReportStatus status, HttpServletRequest request) {
        updateStatus(id, status, request);
        return "redirect:/admin/reports";
    }

    @GetMapping("/status/{id}/{status}")
    public String changeStatusGet(@PathVariable Long id, @PathVariable Report.ReportStatus status, HttpServletRequest request) {
        updateStatus(id, status, request);
        return "redirect:/admin/reports";
    }

    private void updateStatus(Long id, Report.ReportStatus status, HttpServletRequest request) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Signalement introuvable"));
        Report.ReportStatus oldStatus = report.getStatus();
        report.setStatus(status);
        reportRepository.save(report);
        auditLogService.log(AuditAction.REPORT_UPDATED, AuditStatus.SUCCESS, "Report", report.getId(),
                "Changement de statut signalement par l'admin", "status=" + oldStatus, "status=" + report.getStatus(), null, request);
    }
}
