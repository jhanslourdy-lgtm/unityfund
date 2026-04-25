package com.securityapp.gofundme.repositories;

import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.Payment;
import com.securityapp.gofundme.model.PaymentStatus;
import com.securityapp.gofundme.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByDonation_Id(Long donationId);

    List<Payment> findByDonationCampaignUserAndStatus(User user, PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.netAmount), 0) FROM Payment p " +
           "WHERE p.donation.campaign = :campaign AND p.status = :status")
    BigDecimal sumNetAmountByCampaignAndStatus(@Param("campaign") Campaign campaign,
                                               @Param("status") PaymentStatus status);
}
