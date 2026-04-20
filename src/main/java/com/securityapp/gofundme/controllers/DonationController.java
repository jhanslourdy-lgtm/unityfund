/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.Payment;
import com.securityapp.gofundme.repositories.CampaignRepository;
import com.securityapp.gofundme.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class DonationController {

    @Value("${stripe.public.key:}")
    private String stripePublicKey;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("/campaign/{id}/donate")
    public String showDonationForm(@PathVariable Long id, Model model, Principal principal) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));

        model.addAttribute("campaign", campaign);
        model.addAttribute("stripePublicKey", stripePublicKey);
        
        return "donate";
    }

    @GetMapping("/donation/success")
    public String donationSuccess(@RequestParam String transactionId, Model model) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction non trouvée"));
        
        model.addAttribute("payment", payment);
        model.addAttribute("campaign", payment.getDonation().getCampaign());
        return "donation-success";
    }

    @GetMapping("/donation/cancel")
    public String donationCancel() {
        return "donation-cancel";
    }
}
