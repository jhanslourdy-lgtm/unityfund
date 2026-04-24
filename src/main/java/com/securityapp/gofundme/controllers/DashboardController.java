package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.services.CampaignService;
import com.securityapp.gofundme.services.UserService;
import com.securityapp.gofundme.services.WithdrawalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;

@Controller
public class DashboardController {
    
    @Autowired
private WithdrawalService withdrawalService;
    
    @Autowired
    private CampaignService campaignService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        // Récupérer l'utilisateur connecté
        String email = authentication.getName();
        User user = userService.findByEmail(email);

        // CORRECTION : utiliser "user" comme nom de variable (pas "currentUser")
        model.addAttribute("user", user);
        model.addAttribute("campaigns", campaignService.findByUser(user));
        model.addAttribute("totalRaised", campaignService.getTotalRaisedByUser(user));
        
        // Optionnels - si vous les utilisez dans le template
        model.addAttribute("totalDonated", BigDecimal.ZERO); // À calculer si vous avez un service de dons
        model.addAttribute("globalProgress", 0); // À calculer selon votre logique
        model.addAttribute("availableBalance", withdrawalService.getAvailableBalance(user));
        return "dashboard"; // ou "user-Dashboard" selon votre fichier
    }
}