/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.CampaignStatus;
import com.securityapp.gofundme.repositories.CampaignRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author Handy
 */
@Controller
@RequestMapping("/admin/campaigns")
public class AdminCampaignController {

    private final CampaignRepository campaignRepository;

    public AdminCampaignController(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("campaigns", campaignRepository.findAll());
        return "admin/campaigns/list";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        campaignRepository.deleteById(id);
        return "redirect:/admin/campaigns";
    }

    @GetMapping("/status/{id}/{status}")
    public String changeStatus(@PathVariable Long id, @PathVariable CampaignStatus status) {
        Campaign campaign = campaignRepository.findById(id).orElseThrow();
        campaign.setStatus(status);
        campaignRepository.save(campaign);
        return "redirect:/admin/campaigns";
    }
}