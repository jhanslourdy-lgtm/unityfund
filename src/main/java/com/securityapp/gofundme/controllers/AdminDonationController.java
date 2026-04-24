package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.Donation;
import com.securityapp.gofundme.repositories.DonationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@RequestMapping("/admin/donations")
public class AdminDonationController {
    private final DonationRepository donationRepository;

    public AdminDonationController(DonationRepository donationRepository) {
        this.donationRepository = donationRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String list(Model model) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Donation d : donationRepository.findAll()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id", d.getId());
            r.put("amount", d.getAmount());
            r.put("message", d.getMessage());
            r.put("createdAt", d.getCreatedAt());
            r.put("campaignTitle", d.getCampaign() != null ? d.getCampaign().getTitle() : "—");
            r.put("donorEmail", d.getDonor() != null ? d.getDonor().getEmail() : "Anonyme");
            rows.add(r);
        }
        model.addAttribute("donations", rows);
        return "admin/donations/list";
    }
}
