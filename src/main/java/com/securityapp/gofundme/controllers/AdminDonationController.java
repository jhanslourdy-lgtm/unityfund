package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.Donation;
import com.securityapp.gofundme.repositories.DonationRepository;
import com.securityapp.gofundme.services.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/donations")
public class AdminDonationController {

    private final DonationRepository donationRepository;
    private final AuditLogService auditLogService;

    public AdminDonationController(DonationRepository donationRepository, AuditLogService auditLogService) {
        this.donationRepository = donationRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("active", "donations");
        model.addAttribute("donations", donationRepository.findAll());
        return "admin/donations/list";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, Authentication authentication, HttpServletRequest request) {
        Donation donation = donationRepository.findById(id).orElseThrow(() -> new RuntimeException("Don non trouvé"));
        auditLogService.log(authentication, request, "DELETE", "Donation", donation.getId(),
                "amount=" + donation.getAmount(), null);
        donationRepository.delete(donation);
        return "redirect:/admin/donations";
    }
}
