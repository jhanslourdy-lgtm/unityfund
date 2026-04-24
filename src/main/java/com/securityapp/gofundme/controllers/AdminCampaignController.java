package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.CampaignStatus;
import com.securityapp.gofundme.repositories.CampaignRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/campaigns")
public class AdminCampaignController {
    private final CampaignRepository campaignRepository;

    public AdminCampaignController(CampaignRepository campaignRepository) { this.campaignRepository = campaignRepository; }

    @GetMapping
    @Transactional(readOnly = true)
    public String list(Model model) {
        model.addAttribute("campaigns", campaignRepository.findAll());
        model.addAttribute("statuses", CampaignStatus.values());
        return "admin/campaigns/list";
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam CampaignStatus status) {
        Campaign campaign = campaignRepository.findById(id).orElseThrow(() -> new RuntimeException("Campagne introuvable"));
        campaign.setStatus(status);
        campaignRepository.save(campaign);
        return "redirect:/admin/campaigns";
    }
}
