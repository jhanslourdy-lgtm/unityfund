package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.PaymentMethod;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.services.UserService;
import com.securityapp.gofundme.services.WithdrawalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
        User user = getAuthenticatedUser(userDetails);
        prepareWithdrawalListModel(model, user);
        return "withdrawals";
    }

    @GetMapping("/request")
    public String showRequestForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getAuthenticatedUser(userDetails);
        BigDecimal available = withdrawalService.getAvailableBalance(user);
        BigDecimal minAmount = withdrawalService.getMinWithdrawalAmount();

        if (available.compareTo(minAmount) < 0) {
            model.addAttribute("error", "Solde insuffisant pour effectuer un retrait. Minimum requis : " + minAmount + " $.");
            prepareWithdrawalListModel(model, user);
            return "withdrawals";
        }

        model.addAttribute("availableBalance", available);
        model.addAttribute("minAmount", minAmount);
        model.addAttribute("methods", new PaymentMethod[]{PaymentMethod.MONCASH, PaymentMethod.BANK_TRANSFER});
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
            User user = getAuthenticatedUser(userDetails);
            withdrawalService.requestWithdrawal(user, amount, method, recipientPhone, bankName, bankAccountNumber, bankAccountName);
            redirectAttributes.addFlashAttribute("success", "Votre demande de retrait a été envoyée et reste en attente de validation admin.");
            return "redirect:/withdrawals";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/withdrawals/request";
        }
    }

    @PostMapping("/cancel/{id}")
    public String cancelWithdrawal(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        try {
            User user = getAuthenticatedUser(userDetails);
            withdrawalService.cancelWithdrawal(user, id);
            redirectAttributes.addFlashAttribute("success", "La demande de retrait a été annulée.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/withdrawals";
    }

    @GetMapping("/api/balance")
    @ResponseBody
    public ResponseEntity<?> getBalance(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = getAuthenticatedUser(userDetails);
            return ResponseEntity.ok(Map.of(
                    "available", withdrawalService.getAvailableBalance(user),
                    "minAmount", withdrawalService.getMinWithdrawalAmount(),
                    "currency", "USD"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private void prepareWithdrawalListModel(Model model, User user) {
        BigDecimal available = withdrawalService.getAvailableBalance(user);
        BigDecimal minAmount = withdrawalService.getMinWithdrawalAmount();
        model.addAttribute("withdrawals", withdrawalService.getUserWithdrawals(user));
        model.addAttribute("availableBalance", available);
        model.addAttribute("minAmount", minAmount);
        model.addAttribute("canWithdraw", available.compareTo(minAmount) >= 0);
    }

    private User getAuthenticatedUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Vous devez être connecté pour accéder aux retraits.");
        }
        return userService.findByEmail(userDetails.getUsername());
    }
}
