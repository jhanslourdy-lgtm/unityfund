package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.AuditAction;
import com.securityapp.gofundme.model.AuditStatus;
import com.securityapp.gofundme.model.Withdrawal;
import com.securityapp.gofundme.model.WithdrawalStatus;
import com.securityapp.gofundme.repositories.WithdrawalRepository;
import com.securityapp.gofundme.services.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/withdrawals")
public class AdminWithdrawalController {
    private final WithdrawalRepository withdrawalRepository;
    private final AuditLogService auditLogService;

    public AdminWithdrawalController(WithdrawalRepository withdrawalRepository, AuditLogService auditLogService) {
        this.withdrawalRepository = withdrawalRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String list(Model model) {
        model.addAttribute("withdrawals", withdrawalRepository.findAll());
        model.addAttribute("statuses", WithdrawalStatus.values());
        return "admin/withdrawals/list";
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam WithdrawalStatus status, HttpServletRequest request) {
        updateStatus(id, status, request);
        return "redirect:/admin/withdrawals";
    }

    @GetMapping("/approve/{id}")
    public String approve(@PathVariable Long id, HttpServletRequest request) {
        updateStatus(id, WithdrawalStatus.COMPLETED, request);
        return "redirect:/admin/withdrawals";
    }

    @GetMapping("/reject/{id}")
    public String reject(@PathVariable Long id, HttpServletRequest request) {
        updateStatus(id, WithdrawalStatus.REJECTED, request);
        return "redirect:/admin/withdrawals";
    }

    private void updateStatus(Long id, WithdrawalStatus status, HttpServletRequest request) {
        Withdrawal withdrawal = withdrawalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Retrait introuvable"));
        WithdrawalStatus oldStatus = withdrawal.getStatus();
        withdrawal.setStatus(status);
        if (status == WithdrawalStatus.COMPLETED || status == WithdrawalStatus.REJECTED) {
            withdrawal.setProcessedAt(LocalDateTime.now());
        }
        if (status == WithdrawalStatus.REJECTED && (withdrawal.getRejectionReason() == null || withdrawal.getRejectionReason().isBlank())) {
            withdrawal.setRejectionReason("Rejeté par l'administrateur");
        }
        withdrawalRepository.save(withdrawal);
        auditLogService.log(status == WithdrawalStatus.COMPLETED ? AuditAction.WITHDRAWAL_APPROVED : AuditAction.WITHDRAWAL_REJECTED,
                AuditStatus.SUCCESS, "Withdrawal", withdrawal.getId(),
                "Changement de statut retrait par l'admin", "status=" + oldStatus, "status=" + withdrawal.getStatus(), null, request);
    }
}
