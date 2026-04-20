/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.repositories;

import com.securityapp.gofundme.model.Donation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Handy
 */
@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    
    // Lister tous les dons reçus pour une campagne spécifique
    List<Donation> findByCampaignIdOrderByCreatedAtDesc(Long campaignId);
    
    // Lister tous les dons effectués par un utilisateur
    List<Donation> findByDonorId(Long userId);
}
