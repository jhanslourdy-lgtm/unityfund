/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.repositories.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    private final DonationRepository donationRepository;
    private final CategoryRepository categoryRepository;
    private final WithdrawalRepository withdrawalRepository;
    private final ReportRepository reportRepository;

    public AdminController(
            UserRepository userRepository,
            CampaignRepository campaignRepository,
            DonationRepository donationRepository,
            CategoryRepository categoryRepository,
            WithdrawalRepository withdrawalRepository,
            ReportRepository reportRepository
    ) {
        this.userRepository = userRepository;
        this.campaignRepository = campaignRepository;
        this.donationRepository = donationRepository;
        this.categoryRepository = categoryRepository;
        this.withdrawalRepository = withdrawalRepository;
        this.reportRepository = reportRepository;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalCampaigns", campaignRepository.count());
        model.addAttribute("totalDonations", donationRepository.count());
        model.addAttribute("totalCategories", categoryRepository.count());
        model.addAttribute("totalWithdrawals", withdrawalRepository.count());
        model.addAttribute("totalReports", reportRepository.count());

        model.addAttribute("recentUsers", userRepository.findAll());
        model.addAttribute("recentCampaigns", campaignRepository.findAll());
        model.addAttribute("recentDonations", donationRepository.findAll());

        return "admin/dashboard";
    }
}