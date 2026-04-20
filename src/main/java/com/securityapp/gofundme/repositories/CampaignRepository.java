///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.securityapp.gofundme.repositories;
//
//import com.securityapp.gofundme.model.Campaign;
//import com.securityapp.gofundme.model.CampaignStatus;
//import com.securityapp.gofundme.model.User;
//import java.util.List;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
///**
// *
// * @author Handy
// */
//@Repository
//public interface CampaignRepository extends JpaRepository<Campaign, Long> {
//    
//    // Trouver toutes les campagnes d'un créateur spécifique
//    
//    // Trouver les campagnes par statut (ex: afficher uniquement les ACTIVE)
//    List<Campaign> findByStatus(CampaignStatus status);
//    
//    // Trouver les campagnes d'une catégorie particulière
//    List<Campaign> findByUser(User user);
//
//    // Recherche par mot-clé dans le titre (Utile pour une barre de recherche)
//    List<Campaign> findByTitleContainingIgnoreCase(String keyword);
//    // Trouve toutes les campagnes d'un utilisateur spécifique
//    List<Campaign> findByTitleContainingIgnoreCaseAndStatus(String keyword, CampaignStatus status);
//List<Campaign> findByCategory_NameIgnoreCaseAndStatus(String categoryName, CampaignStatus status);
//
//   
//}
package com.securityapp.gofundme.repositories;

import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.CampaignStatus;
import com.securityapp.gofundme.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    
    // Méthodes existantes (gardez-les)
    List<Campaign> findByStatus(CampaignStatus status);
    List<Campaign> findByUser(User user);
    List<Campaign> findByTitleContainingIgnoreCase(String keyword);
    List<Campaign> findByTitleContainingIgnoreCaseAndStatus(String keyword, CampaignStatus status);
    List<Campaign> findByCategory_NameIgnoreCaseAndStatus(String categoryName, CampaignStatus status);
    
    // ========== NOUVELLES MÉTHODES PAGINÉES ==========
    
    // Pagination de base - toutes les campagnes actives
    Page<Campaign> findByStatus(CampaignStatus status, Pageable pageable);
    
    // Pagination avec recherche par titre
    Page<Campaign> findByTitleContainingIgnoreCaseAndStatus(String keyword, CampaignStatus status, Pageable pageable);
    
    // Pagination par catégorie
    Page<Campaign> findByCategory_NameIgnoreCaseAndStatus(String categoryName, CampaignStatus status, Pageable pageable);
    
    // Pagination pour les campagnes d'un utilisateur
    Page<Campaign> findByUser(User user, Pageable pageable);
}