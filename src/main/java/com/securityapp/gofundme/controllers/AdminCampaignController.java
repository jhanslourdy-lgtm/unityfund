package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.AuditAction;
import com.securityapp.gofundme.model.AuditStatus;
import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.CampaignStatus;
import com.securityapp.gofundme.repositories.CampaignRepository;
import com.securityapp.gofundme.services.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/campaigns")
public class AdminCampaignController {
    private final CampaignRepository campaignRepository;
    private final AuditLogService auditLogService;

    public AdminCampaignController(CampaignRepository campaignRepository, AuditLogService auditLogService) {
        this.campaignRepository = campaignRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String list(Model model) {
        model.addAttribute("campaigns", campaignRepository.findAll());
        model.addAttribute("statuses", CampaignStatus.values());
        return "admin/campaigns/list";
    }

    @PostMapping("/{id}/status")
    public String changeStatusPost(@PathVariable Long id, @RequestParam CampaignStatus status, HttpServletRequest request) {
        updateStatus(id, status, request);
        return "redirect:/admin/campaigns";
    }

    @GetMapping("/status/{id}/{status}")
    public String changeStatusGet(@PathVariable Long id, @PathVariable CampaignStatus status, HttpServletRequest request) {
        updateStatus(id, status, request);
        return "redirect:/admin/campaigns";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpServletRequest request) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campagne introuvable"));
        String oldValue = "status=" + campaign.getStatus() + ", title=" + campaign.getTitle();
        campaign.setStatus(CampaignStatus.DELETED);
        campaignRepository.save(campaign);
        auditLogService.log(AuditAction.CAMPAIGN_DELETED, AuditStatus.SUCCESS, "Campaign", campaign.getId(),
                "Campagne supprimée logiquement par l'admin", oldValue,
                "status=" + campaign.getStatus() + ", title=" + campaign.getTitle(), null, request);
        return "redirect:/admin/campaigns";
    }

    private void updateStatus(Long id, CampaignStatus status, HttpServletRequest request) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campagne introuvable"));
        CampaignStatus oldStatus = campaign.getStatus();
        campaign.setStatus(status);
        campaignRepository.save(campaign);
        auditLogService.log(AuditAction.CAMPAIGN_UPDATED, AuditStatus.SUCCESS, "Campaign", campaign.getId(),
                "Changement de statut campagne par l'admin",
                "status=" + oldStatus, "status=" + campaign.getStatus(), null, request);
    }
}
