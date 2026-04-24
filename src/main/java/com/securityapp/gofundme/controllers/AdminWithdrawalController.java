package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.Withdrawal;
import com.securityapp.gofundme.model.WithdrawalStatus;
import com.securityapp.gofundme.repositories.WithdrawalRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/withdrawals")
public class AdminWithdrawalController {
    private final WithdrawalRepository withdrawalRepository;

    public AdminWithdrawalController(WithdrawalRepository withdrawalRepository) { this.withdrawalRepository = withdrawalRepository; }

    @GetMapping
    @Transactional(readOnly = true)
    public String list(Model model) {
        model.addAttribute("withdrawals", withdrawalRepository.findAll());
        model.addAttribute("statuses", WithdrawalStatus.values());
        return "admin/withdrawals/list";
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam WithdrawalStatus status) {
        Withdrawal withdrawal = withdrawalRepository.findById(id).orElseThrow(() -> new RuntimeException("Retrait introuvable"));
        withdrawal.setStatus(status);
        if (status == WithdrawalStatus.COMPLETED || status == WithdrawalStatus.REJECTED) {
            withdrawal.setProcessedAt(LocalDateTime.now());
        }
        withdrawalRepository.save(withdrawal);
        return "redirect:/admin/withdrawals";
    }
}
