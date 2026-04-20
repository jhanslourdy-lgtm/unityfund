/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.services;

import com.securityapp.gofundme.dto.LikeResponse;
import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.CampaignLike;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.repositories.CampaignLikeRepository;
import com.securityapp.gofundme.repositories.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CampaignLikeService {

    @Autowired
    private CampaignLikeRepository likeRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Transactional
    public LikeResponse toggleLike(User user, Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));

        Optional<CampaignLike> existing = likeRepository.findByUserAndCampaign(user, campaign);
        boolean liked;

        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            liked = false;
        } else {
            CampaignLike like = new CampaignLike();
            like.setUser(user);
            like.setCampaign(campaign);
            likeRepository.save(like);
            liked = true;
        }

        long count = likeRepository.countByCampaign(campaign);
        return new LikeResponse(liked, count);
    }

    public long countLikes(Long campaignId) {
        return campaignRepository.findById(campaignId)
                .map(likeRepository::countByCampaign)
                .orElse(0L);
    }

    public boolean isLikedByUser(User user, Long campaignId) {
        return campaignRepository.findById(campaignId)
                .map(c -> likeRepository.existsByUserAndCampaign(user, c))
                .orElse(false);
    }
}