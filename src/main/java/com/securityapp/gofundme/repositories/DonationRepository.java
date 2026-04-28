package com.securityapp.gofundme.repositories;

import com.securityapp.gofundme.model.Donation;
import com.securityapp.gofundme.model.DonationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByCampaignIdOrderByCreatedAtDesc(Long campaignId);

    List<Donation> findByCampaignIdAndStatusOrderByCreatedAtDesc(Long campaignId, DonationStatus status);

    List<Donation> findByDonorId(Long userId);

    List<Donation> findByDonorIdAndStatusOrderByCreatedAtDesc(Long userId, DonationStatus status);
}
