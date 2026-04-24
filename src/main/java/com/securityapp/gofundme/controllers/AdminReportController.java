package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.Report;
import com.securityapp.gofundme.repositories.ReportRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {
    private final ReportRepository reportRepository;

    public AdminReportController(ReportRepository reportRepository) { this.reportRepository = reportRepository; }

    @GetMapping
    @Transactional(readOnly = true)
    public String list(Model model) {
        model.addAttribute("reports", reportRepository.findAll());
        return "admin/reports/list";
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam Report.ReportStatus status) {
        Report report = reportRepository.findById(id).orElseThrow(() -> new RuntimeException("Signalement introuvable"));
        report.setStatus(status);
        reportRepository.save(report);
        return "redirect:/admin/reports";
    }
}
