package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.AuditAction;
import com.securityapp.gofundme.model.AuditStatus;
import com.securityapp.gofundme.model.Donation;
import com.securityapp.gofundme.model.Payment;
import com.securityapp.gofundme.repositories.DonationRepository;
import com.securityapp.gofundme.repositories.PaymentRepository;
import com.securityapp.gofundme.services.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/donations")
public class AdminDonationController {
    private final DonationRepository donationRepository;
    private final PaymentRepository paymentRepository;
    private final AuditLogService auditLogService;

    public AdminDonationController(DonationRepository donationRepository, PaymentRepository paymentRepository, AuditLogService auditLogService) {
        this.donationRepository = donationRepository;
        this.paymentRepository = paymentRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String list(Model model) {
        model.addAttribute("donations", donationRepository.findAll());
        return "admin/donations/list";
    }

    @GetMapping("/delete/{id}")
    @Transactional
    public String delete(@PathVariable Long id, HttpServletRequest request) {
        Donation donation = donationRepository.findById(id).orElseThrow(() -> new RuntimeException("Don introuvable"));
        String oldValue = "amount=" + donation.getAmount()
                + ", campaign=" + (donation.getCampaign() != null ? donation.getCampaign().getId() : null)
                + ", donor=" + (donation.getDonor() != null ? donation.getDonor().getEmail() : "ANONYMOUS");
        List<Payment> payments = paymentRepository.findByDonationId(id);
        if (!payments.isEmpty()) paymentRepository.deleteAll(payments);
        donationRepository.delete(donation);
        auditLogService.log(AuditAction.ADMIN_ACTION, AuditStatus.SUCCESS, "Donation", id,
                "Don supprimé par l'admin", oldValue, null, null, request);
        return "redirect:/admin/donations";
    }
}
