/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.services;


import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.CampaignStatus;
import com.securityapp.gofundme.model.Donation;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.repositories.CampaignRepository;
import com.securityapp.gofundme.repositories.DonationRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Handy
 */
@Service
public class DonationService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CampaignRepository campaignRepository;
public void processDonation(Long campaignId, Donation donation, User donor) {
    donation.setDonor(donor); // Lier l'utilisateur connecté
}
    @Transactional // Garantit l'intégrité des données (Tout ou rien)
    public void processDonation(Long campaignId, Donation donation) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campagne introuvable"));

        // 1. Enregistrer le don
        donation.setCampaign(campaign);
        donationRepository.save(donation);

        // 2. Mettre à jour le montant de la campagne
        BigDecimal newAmount = campaign.getCurrentAmount().add(donation.getAmount());
        campaign.setCurrentAmount(newAmount);
        
        // 3. Vérifier si l'objectif est atteint pour changer le statut
        if (newAmount.compareTo(campaign.getGoalAmount()) >= 0) {
            campaign.setStatus(CampaignStatus.COMPLETED);
        }

        campaignRepository.save(campaign);
    }
}