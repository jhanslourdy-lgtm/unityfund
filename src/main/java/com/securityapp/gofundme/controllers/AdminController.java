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

    public AdminController(UserRepository userRepository,
                           CampaignRepository campaignRepository,
                           DonationRepository donationRepository,
                           CategoryRepository categoryRepository,
                           WithdrawalRepository withdrawalRepository,
                           ReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.campaignRepository = campaignRepository;
        this.donationRepository = donationRepository;
        this.categoryRepository = categoryRepository;
        this.withdrawalRepository = withdrawalRepository;
        this.reportRepository = reportRepository;
    }

    @GetMapping({"/admin", "/admin/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", safeCount(() -> userRepository.count()));
        model.addAttribute("totalCampaigns", safeCount(() -> campaignRepository.count()));
        model.addAttribute("totalDonations", safeCount(() -> donationRepository.count()));
        model.addAttribute("totalCategories", safeCount(() -> categoryRepository.count()));
        model.addAttribute("totalWithdrawals", safeCount(() -> withdrawalRepository.count()));
        model.addAttribute("totalReports", safeCount(() -> reportRepository.count()));
        return "admin/dashboard";
    }

    private long safeCount(CountSupplier supplier) {
        try { return supplier.get(); } catch (Exception e) { return 0L; }
    }

    @FunctionalInterface
    private interface CountSupplier { long get(); }
}
