package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.Withdrawal;
import com.securityapp.gofundme.repositories.WithdrawalRepository;
import com.securityapp.gofundme.services.AuditLogService;
import com.securityapp.gofundme.services.WithdrawalService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/withdrawals")
public class AdminWithdrawalController {

    private final WithdrawalRepository withdrawalRepository;
    private final WithdrawalService withdrawalService;
    private final AuditLogService auditLogService;

    public AdminWithdrawalController(WithdrawalRepository withdrawalRepository,
                                     WithdrawalService withdrawalService,
                                     AuditLogService auditLogService) {
        this.withdrawalRepository = withdrawalRepository;
        this.withdrawalService = withdrawalService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("active", "withdrawals");
        model.addAttribute("withdrawals", withdrawalRepository.findAll());
        return "admin/withdrawals/list";
    }

    @GetMapping("/approve/{id}")
    public String approve(@PathVariable Long id, Authentication authentication, HttpServletRequest request) {
        Withdrawal withdrawal = withdrawalRepository.findById(id).orElseThrow(() -> new RuntimeException("Retrait non trouvé"));
        String oldStatus = withdrawal.getStatus().name();
        withdrawalService.approveWithdrawal(id);
        auditLogService.log(authentication, request, "WITHDRAWAL_APPROVED", "Withdrawal", id, oldStatus, "COMPLETED");
        return "redirect:/admin/withdrawals";
    }

    @GetMapping("/reject/{id}")
    public String reject(@PathVariable Long id, Authentication authentication, HttpServletRequest request) {
        Withdrawal withdrawal = withdrawalRepository.findById(id).orElseThrow(() -> new RuntimeException("Retrait non trouvé"));
        String oldStatus = withdrawal.getStatus().name();
        withdrawalService.rejectWithdrawal(id, "Rejeté par l'administrateur");
        auditLogService.log(authentication, request, "WITHDRAWAL_REJECTED", "Withdrawal", id, oldStatus, "REJECTED");
        return "redirect:/admin/withdrawals";
    }
}
