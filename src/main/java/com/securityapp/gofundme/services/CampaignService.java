///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.securityapp.gofundme.services;
//
//import com.securityapp.gofundme.model.Campaign;
//import com.securityapp.gofundme.model.CampaignStatus;
//import com.securityapp.gofundme.model.User;
//import com.securityapp.gofundme.repositories.CampaignRepository;
//import java.math.BigDecimal;
//import java.util.List;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
///**
// *
// * @author Handy
// */
//@Service
//public class CampaignService {
//
//    @Autowired
//    private CampaignRepository campaignRepository;
//
//    public Campaign createCampaign(Campaign campaign, User creator) {
//        // Validation métier simple
//        if (campaign.getGoalAmount().compareTo(BigDecimal.ZERO) <= 0) {
//            throw new IllegalArgumentException("L'objectif doit être supérieur à zéro");
//        }
//        
//        campaign.setUser(user); // Utilise le nom qui correspond à ton champ @ManyToOne private User user;
//        campaign.setCurrentAmount(BigDecimal.ZERO);
//        
//        return campaignRepository.save(campaign);
//    }
//    public List<Campaign> findAllActive() {
//    return campaignRepository.findByStatus(CampaignStatus.ACTIVE);
//}
//
//    // Enregistre ou met à jour une campagne
//    // Remplace "ACTIVE" (String) par la valeur de l'Enum
//// APRÈS (Correction)
//public void save(Campaign campaign, User user) {
//    campaign.setUser(user); // Maintenant 'user' est reconnu car il est dans les paramètres
//    if (campaign.getStatus() == null) {
//        campaign.setStatus(CampaignStatus.ACTIVE);
//    }
//    campaignRepository.save(campaign);
//}
//
//    // Récupère toutes les campagnes pour l'accueil
//    
//    // Récupère les campagnes d'un utilisateur pour le Dashboard
//    public List<Campaign> findByUser(User user) {
//        return campaignRepository.findByUser(user);
//    }
//
//    // Calcule le total récolté par un utilisateur (exemple de logique métier)
//    
//
//public BigDecimal getTotalRaisedByUser(User user) {
//    return campaignRepository.findByUser(user)
//            .stream()
//            .map(Campaign::getCurrentAmount) // Récupère le BigDecimal
//            .reduce(BigDecimal.ZERO, BigDecimal::add); // Additionne les BigDecimal
//}
//}
package com.securityapp.gofundme.services;

import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.CampaignStatus;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.repositories.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service pour la gestion des campagnes de financement.
 * Gère la logique métier et l'accès au repository.
 */
@Service
public class CampaignService {

    @Autowired
    private CampaignRepository campaignRepository;

    /**
     * Enregistre une nouvelle campagne en la liant à son créateur.
     * @param campaign L'objet campagne provenant du formulaire
     * @param user L'utilisateur actuellement connecté
     */
    public void save(Campaign campaign, User user) {
        // Liaison de la campagne à l'utilisateur
        campaign.setUser(user); 
        
        // Initialisation du montant actuel à zéro si c'est une nouvelle campagne
        if (campaign.getCurrentAmount() == null) {
            campaign.setCurrentAmount(BigDecimal.ZERO);
        }
        
        // Définition du statut par défaut
        if (campaign.getStatus() == null) {
            campaign.setStatus(CampaignStatus.ACTIVE);
        }
        
        campaignRepository.save(campaign);
    }

    /**
     * Récupère toutes les campagnes actives pour la page d'accueil.
     */
    public List<Campaign> findAllActive() {
        return campaignRepository.findByStatus(CampaignStatus.ACTIVE);
    }

    /**
     * Récupère les campagnes d'un utilisateur spécifique pour son dashboard.
     */
    public List<Campaign> findByUser(User user) {
        return campaignRepository.findByUser(user);
    }

    /**
     * Calcule le montant total récolté par un utilisateur sur toutes ses campagnes.
     */
    public BigDecimal getTotalRaisedByUser(User user) {
        return campaignRepository.findByUser(user)
                .stream()
                .map(Campaign::getCurrentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}