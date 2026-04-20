/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.repositories;

import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.CampaignLike;
import com.securityapp.gofundme.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignLikeRepository extends JpaRepository<CampaignLike, Long> {
    Optional<CampaignLike> findByUserAndCampaign(User user, Campaign campaign);
    long countByCampaign(Campaign campaign);
    boolean existsByUserAndCampaign(User user, Campaign campaign);
    List<CampaignLike> findByUser(User user);
    void deleteByUserAndCampaign(User user, Campaign campaign);
}