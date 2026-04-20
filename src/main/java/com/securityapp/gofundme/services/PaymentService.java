package com.securityapp.gofundme.services;

import com.securityapp.gofundme.dto.DonationRequest;
import com.securityapp.gofundme.dto.PaymentIntent;
import com.securityapp.gofundme.model.*;
import com.securityapp.gofundme.repositories.CampaignRepository;
import com.securityapp.gofundme.repositories.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.securityapp.gofundme.repositories.DonationRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {
       
    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private StripePaymentProvider stripeProvider;
    
    @Autowired
    private MonCashPaymentProvider moncashProvider;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private CampaignRepository campaignRepository;
    
    @Value("${platform.fee.percentage:0.05}")
    private BigDecimal platformFeePct;
    
    @Transactional
    public PaymentIntent createPaymentIntent(DonationRequest request, User donor) throws Exception {
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
            .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));
            
        if (campaign.getStatus() != CampaignStatus.ACTIVE) {
            throw new RuntimeException("Cette campagne n'est plus active");
        }
        
        BigDecimal amount = request.getAmount();
        BigDecimal platformFee = amount.multiply(platformFeePct);
        BigDecimal processingFee = calculateProcessingFee(amount, request.getMethod());
        BigDecimal netAmount = amount.subtract(platformFee).subtract(processingFee);
        
        Donation donation = new Donation();
        donation.setAmount(amount);
        donation.setCampaign(campaign);
        donation.setDonor(donor);
        donation.setMessage(request.getMessage());
        donationRepository.save(donation);
        
        Payment payment = new Payment();
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setAmount(amount);
        payment.setPlatformFee(platformFee);
        payment.setProcessingFee(processingFee);
        payment.setNetAmount(netAmount);
        payment.setMethod(request.getMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setDonation(donation);
        
        PaymentIntent intent;
        switch (request.getMethod()) {
            case STRIPE:
                intent = stripeProvider.createIntent(payment, campaign, donor);
                break;
            case MONCASH:
                intent = moncashProvider.createIntent(payment, campaign, donor);
                break;
            default:
                throw new IllegalArgumentException("Méthode non supportée");
        }
        
        payment.setProviderResponse(intent.getProviderJson());
        paymentRepository.save(payment);
        
        return intent;
    }
    
    @Transactional
    public void confirmPayment(String transactionId, String providerResponse) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction non trouvée"));
            
        if (payment.getStatus() == PaymentStatus.COMPLETED) return;
        
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setProviderResponse(providerResponse);
        
        Campaign campaign = payment.getDonation().getCampaign();
        campaign.setCurrentAmount(campaign.getCurrentAmount().add(payment.getAmount()));
        
        if (campaign.getCurrentAmount().compareTo(campaign.getGoalAmount()) >= 0) {
            campaign.setStatus(CampaignStatus.COMPLETED);
        }
        
        campaignRepository.save(campaign);
        paymentRepository.save(payment);
    }
    
    private BigDecimal calculateProcessingFee(BigDecimal amount, PaymentMethod method) {
        switch (method) {
            case STRIPE:
                return amount.multiply(new BigDecimal("0.029")).add(new BigDecimal("0.30"));
            case MONCASH:
                return amount.multiply(new BigDecimal("0.02"));
            default:
                return BigDecimal.ZERO;
        }
    }
}