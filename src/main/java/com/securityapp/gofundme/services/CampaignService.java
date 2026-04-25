package com.securityapp.gofundme.services;

import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.CampaignStatus;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.repositories.CampaignRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CampaignService {

    @Autowired
    private CampaignRepository campaignRepository;

    public void save(Campaign campaign, User user) {
        campaign.setUser(user);

        if (campaign.getCurrentAmount() == null) {
            campaign.setCurrentAmount(BigDecimal.ZERO);
        }

        if (campaign.getStatus() == null) {
            campaign.setStatus(CampaignStatus.ACTIVE);
        }

        campaignRepository.save(campaign);
    }

    public Campaign findById(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campagne introuvable"));
    }

    public List<Campaign> findAllActive() {
        return campaignRepository.findByStatus(CampaignStatus.ACTIVE);
    }

    public List<Campaign> findByUser(User user) {
        return campaignRepository.findByUser(user);
    }

    public BigDecimal getTotalRaisedByUser(User user) {
        return campaignRepository.findByUser(user)
                .stream()
                .map(Campaign::getCurrentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
