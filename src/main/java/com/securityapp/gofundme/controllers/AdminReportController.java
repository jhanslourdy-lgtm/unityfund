package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.AuditAction;
import com.securityapp.gofundme.model.AuditStatus;
import com.securityapp.gofundme.model.Report;
import com.securityapp.gofundme.repositories.ReportRepository;
import com.securityapp.gofundme.services.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    private final ReportRepository reportRepository;
    private final AuditLogService auditLogService;

    public AdminReportController(
            ReportRepository reportRepository,
            AuditLogService auditLogService
    ) {
        this.reportRepository = reportRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("reports", reportRepository.findAll());
        return "admin/reports/list";
    }

    @GetMapping("/review/{id}")
    public String review(@PathVariable Long id, HttpServletRequest request) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Signalement introuvable"));

        String oldValue = "status=" + report.getStatus();

        report.setStatus(Report.ReportStatus.REVIEWED);
        reportRepository.save(report);

        String newValue = "status=" + report.getStatus();

        auditLogService.log(
                AuditAction.REPORT_UPDATED,
                AuditStatus.SUCCESS,
                "Report",
                report.getId(),
                "Signalement mis en examen par l'admin",
                oldValue,
                newValue,
                null,
                request
        );

        return "redirect:/admin/reports";
    }

    @GetMapping("/resolve/{id}")
    public String resolve(@PathVariable Long id, HttpServletRequest request) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Signalement introuvable"));

        String oldValue = "status=" + report.getStatus();

        report.setStatus(Report.ReportStatus.RESOLVED);
        reportRepository.save(report);

        String newValue = "status=" + report.getStatus();

        auditLogService.log(
                AuditAction.REPORT_UPDATED,
                AuditStatus.SUCCESS,
                "Report",
                report.getId(),
                "Signalement résolu par l'admin",
                oldValue,
                newValue,
                null,
                request
        );

        return "redirect:/admin/reports";
    }

    @GetMapping("/dismiss/{id}")
    public String dismiss(@PathVariable Long id, HttpServletRequest request) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Signalement introuvable"));

        String oldValue = "status=" + report.getStatus();

        report.setStatus(Report.ReportStatus.DISMISSED);
        reportRepository.save(report);

        String newValue = "status=" + report.getStatus();

        auditLogService.log(
                AuditAction.REPORT_UPDATED,
                AuditStatus.SUCCESS,
                "Report",
                report.getId(),
                "Signalement rejeté par l'admin",
                oldValue,
                newValue,
                null,
                request
        );

        return "redirect:/admin/reports";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpServletRequest request) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Signalement introuvable"));

        String oldValue = "status=" + report.getStatus()
                + ", reason=" + report.getReason()
                + ", description=" + report.getDescription();

        auditLogService.log(
                AuditAction.REPORT_DELETED,
                AuditStatus.SUCCESS,
                "Report",
                report.getId(),
                "Signalement supprimé par l'admin",
                oldValue,
                null,
                null,
                request
        );

        reportRepository.delete(report);

        return "redirect:/admin/reports";
    }
}