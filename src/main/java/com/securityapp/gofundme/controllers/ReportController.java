/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.Report;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.repositories.CampaignRepository;
import com.securityapp.gofundme.repositories.ReportRepository;
import com.securityapp.gofundme.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/campaigns/{campaignId}/report")
public class ReportController {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> reportCampaign(@PathVariable Long campaignId,
                                            @RequestBody Map<String, String> payload,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Connexion requise"));
        }

        try {
            User reporter = userService.findByEmail(userDetails.getUsername());

            // Vérifier si déjà signalé
            if (reportRepository.existsByCampaignIdAndReporterId(campaignId, reporter.getId())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vous avez déjà signalé cette campagne"));
            }

            Report report = new Report();
            report.setCampaign(campaignRepository.findById(campaignId)
                    .orElseThrow(() -> new RuntimeException("Campagne non trouvée")));
            report.setReporter(reporter);
            report.setReason(Report.ReportReason.valueOf(payload.get("reason")));
            report.setDescription(payload.getOrDefault("description", ""));

            reportRepository.save(report);

            return ResponseEntity.ok(Map.of("success", true, "message", "Signalement envoyé. Merci !"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Raison invalide"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}