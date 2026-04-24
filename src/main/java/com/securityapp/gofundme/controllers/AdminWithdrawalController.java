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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/withdrawals")
public class AdminWithdrawalController {

    private final WithdrawalRepository withdrawalRepository;
    private final AuditLogService auditLogService;

    public AdminWithdrawalController(
            WithdrawalRepository withdrawalRepository,
            AuditLogService auditLogService
    ) {
        this.withdrawalRepository = withdrawalRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("withdrawals", withdrawalRepository.findAll());
        return "admin/withdrawals/list";
    }

    @GetMapping("/approve/{id}")
    public String approve(@PathVariable Long id, HttpServletRequest request) {
        Withdrawal withdrawal = withdrawalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Retrait introuvable"));

        String oldValue = "status=" + withdrawal.getStatus()
                + ", amount=" + withdrawal.getAmount();

        withdrawal.setStatus(WithdrawalStatus.COMPLETED);
        withdrawal.setProcessedAt(LocalDateTime.now());
        withdrawalRepository.save(withdrawal);

        String newValue = "status=" + withdrawal.getStatus()
                + ", amount=" + withdrawal.getAmount();

        auditLogService.log(
                AuditAction.WITHDRAWAL_APPROVED,
                AuditStatus.SUCCESS,
                "Withdrawal",
                withdrawal.getId(),
                "Retrait approuvé par l'admin",
                oldValue,
                newValue,
                null,
                request
        );

        return "redirect:/admin/withdrawals";
    }

    @GetMapping("/reject/{id}")
    public String reject(@PathVariable Long id, HttpServletRequest request) {
        Withdrawal withdrawal = withdrawalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Retrait introuvable"));

        String oldValue = "status=" + withdrawal.getStatus()
                + ", amount=" + withdrawal.getAmount();

        withdrawal.setStatus(WithdrawalStatus.REJECTED);
        withdrawal.setProcessedAt(LocalDateTime.now());
        withdrawal.setRejectionReason("Rejeté par l'administrateur");
        withdrawalRepository.save(withdrawal);

        String newValue = "status=" + withdrawal.getStatus()
                + ", amount=" + withdrawal.getAmount()
                + ", rejectionReason=" + withdrawal.getRejectionReason();

        auditLogService.log(
                AuditAction.WITHDRAWAL_REJECTED,
                AuditStatus.SUCCESS,
                "Withdrawal",
                withdrawal.getId(),
                "Retrait rejeté par l'admin",
                oldValue,
                newValue,
                null,
                request
        );

        return "redirect:/admin/withdrawals";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpServletRequest request) {
        Withdrawal withdrawal = withdrawalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Retrait introuvable"));

        String oldValue = "status=" + withdrawal.getStatus()
                + ", amount=" + withdrawal.getAmount()
                + ", method=" + withdrawal.getMethod();

        auditLogService.log(
                AuditAction.ADMIN_ACTION,
                AuditStatus.SUCCESS,
                "Withdrawal",
                withdrawal.getId(),
                "Retrait supprimé par l'admin",
                oldValue,
                null,
                null,
                request
        );

        withdrawalRepository.delete(withdrawal);

        return "redirect:/admin/withdrawals";
    }
}