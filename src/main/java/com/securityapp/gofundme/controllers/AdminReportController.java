package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.Report;
import com.securityapp.gofundme.repositories.ReportRepository;
import com.securityapp.gofundme.services.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String list(Model model) {
        model.addAttribute("active", "reports");
        model.addAttribute("reports", reportRepository.findAll());
        return "admin/reports/list";
    }

    @GetMapping("/status/{id}/{status}")
    public String changeStatus(@PathVariable Long id,
                               @PathVariable Report.ReportStatus status,
                               Authentication authentication,
                               HttpServletRequest request) {
        Report report = reportRepository.findById(id).orElseThrow(() -> new RuntimeException("Signalement non trouvé"));
        String oldStatus = report.getStatus().name();
        report.setStatus(status);
        reportRepository.save(report);
        auditLogService.log(authentication, request, "REPORT_STATUS_CHANGED", "Report", id, oldStatus, status.name());
        return "redirect:/admin/reports";
    }
}
