package com.securityapp.gofundme.repositories;

import com.securityapp.gofundme.model.Payment;
import com.securityapp.gofundme.model.PaymentStatus;
import com.securityapp.gofundme.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);
    List<Payment> findByDonationCampaignUserAndStatus(User user, PaymentStatus status);
    List<Payment> findByDonationId(Long donationId);
}
