/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.PaymentMethod;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.model.Withdrawal;
import com.securityapp.gofundme.services.UserService;
import com.securityapp.gofundme.services.WithdrawalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/withdrawals")
public class WithdrawalController {

    @Autowired
    private WithdrawalService withdrawalService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listWithdrawals(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        model.addAttribute("withdrawals", withdrawalService.getUserWithdrawals(user));
        model.addAttribute("availableBalance", withdrawalService.getAvailableBalance(user));
        model.addAttribute("minAmount", new BigDecimal("10.00"));
        return "withdrawals";
    }

    @GetMapping("/request")
    public String showRequestForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        BigDecimal available = withdrawalService.getAvailableBalance(user);
        
        if (available.compareTo(new BigDecimal("10.00")) < 0) {
            model.addAttribute("error", "Solde insuffisant pour effectuer un retrait (minimum 10 $).");
            return "withdrawals";
        }
        
        model.addAttribute("availableBalance", available);
        return "withdrawal-request";
    }

    @PostMapping("/request")
    public String processRequest(@RequestParam BigDecimal amount,
                                 @RequestParam PaymentMethod method,
                                 @RequestParam(required = false) String recipientPhone,
                                 @RequestParam(required = false) String bankName,
                                 @RequestParam(required = false) String bankAccountNumber,
                                 @RequestParam(required = false) String bankAccountName,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            withdrawalService.requestWithdrawal(user, amount, method, recipientPhone, bankName, bankAccountNumber, bankAccountName);
            redirectAttributes.addFlashAttribute("success", "Votre demande de retrait a été envoyée et est en attente de validation.");
            return "redirect:/withdrawals";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/withdrawals/request";
        }
    }

    // API pour récupérer le solde en JSON (utile pour le dashboard)
    @GetMapping("/api/balance")
    @ResponseBody
    public ResponseEntity<?> getBalance(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            return ResponseEntity.ok(Map.of(
                "available", withdrawalService.getAvailableBalance(user),
                "currency", "USD"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}