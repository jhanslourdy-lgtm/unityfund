package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.PaymentMethod;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.services.CampaignService;
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
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/withdrawals")
public class WithdrawalController {

    @Autowired
    private WithdrawalService withdrawalService;

    @Autowired
    private UserService userService;

    @Autowired
    private CampaignService campaignService;

    @GetMapping
    public String listWithdrawals(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<Campaign> campaigns = campaignService.findByUser(user);

        model.addAttribute("user", user);
        model.addAttribute("campaigns", campaigns);
        model.addAttribute("withdrawals", withdrawalService.getUserWithdrawals(user));
        model.addAttribute("availableBalance", withdrawalService.getAvailableBalance(user));
        model.addAttribute("minAmount", new BigDecimal("10.00"));
        return "withdrawals";
    }

    @GetMapping("/request")
    public String showRequestFormWithoutCampaign(Model model,
                                                 @AuthenticationPrincipal UserDetails userDetails,
                                                 RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<Campaign> campaigns = campaignService.findByUser(user);

        Campaign firstEligible = campaigns.stream()
                .filter(c -> withdrawalService.getAvailableBalanceForCampaign(c).compareTo(new BigDecimal("10.00")) >= 0)
                .findFirst()
                .orElse(null);

        if (firstEligible == null) {
            redirectAttributes.addFlashAttribute("error", "Aucune campagne ne possède un solde retirable pour le moment.");
            return "redirect:/dashboard";
        }

        return "redirect:/withdrawals/request/" + firstEligible.getId();
    }

    @GetMapping("/request/{campaignId}")
    public String showRequestForm(@PathVariable Long campaignId,
                                  Model model,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername());
        Campaign campaign = campaignService.findById(campaignId);

        if (campaign == null || campaign.getUser() == null || !campaign.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Campagne introuvable ou non autorisée.");
            return "redirect:/dashboard";
        }

        BigDecimal available = withdrawalService.getAvailableBalanceForCampaign(campaign);
        if (available.compareTo(new BigDecimal("10.00")) < 0) {
            redirectAttributes.addFlashAttribute("error", "Solde insuffisant pour effectuer un retrait sur cette campagne (minimum 10 $).");
            return "redirect:/dashboard";
        }

        model.addAttribute("user", user);
        model.addAttribute("campaign", campaign);
        model.addAttribute("availableBalance", available);
        model.addAttribute("minAmount", new BigDecimal("10.00"));
        return "withdrawal-request";
    }

    @PostMapping("/request")
    public String processRequest(@RequestParam Long campaignId,
                                 @RequestParam BigDecimal amount,
                                 @RequestParam PaymentMethod method,
                                 @RequestParam(required = false) String recipientPhone,
                                 @RequestParam(required = false) String bankName,
                                 @RequestParam(required = false) String bankAccountNumber,
                                 @RequestParam(required = false) String bankAccountName,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            withdrawalService.requestWithdrawal(user, campaignId, amount, method,
                    recipientPhone, bankName, bankAccountNumber, bankAccountName);
            redirectAttributes.addFlashAttribute("success", "Votre demande de retrait a été envoyée. La campagne concernée est maintenant fermée à la collecte.");
            return "redirect:/withdrawals";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/withdrawals/request/" + campaignId;
        }
    }

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
