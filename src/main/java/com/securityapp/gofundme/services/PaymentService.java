//package com.securityapp.gofundme.services;
//
//import com.securityapp.gofundme.dto.DonationRequest;
//import com.securityapp.gofundme.dto.PaymentIntent;
//import com.securityapp.gofundme.model.*;
//import com.securityapp.gofundme.repositories.CampaignRepository;
//import com.securityapp.gofundme.repositories.PaymentRepository;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import com.securityapp.gofundme.repositories.DonationRepository;
//
//import java.math.BigDecimal;
//import java.util.UUID;
//
//@Service
//public class PaymentService {
//       
//    @Autowired
//    private DonationRepository donationRepository;
//
//    @Autowired
//    private StripePaymentProvider stripeProvider;
//    
//    @Autowired
//    private MonCashPaymentProvider moncashProvider;
//    
//    @Autowired
//    private PaymentRepository paymentRepository;
//    
//    @Autowired
//    private CampaignRepository campaignRepository;
//    
//    @Value("${platform.fee.percentage:0.05}")
//    private BigDecimal platformFeePct;
//    
//    @Transactional
//    public PaymentIntent createPaymentIntent(DonationRequest request, User donor) throws Exception {
//        Campaign campaign = campaignRepository.findById(request.getCampaignId())
//            .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));
//            
//        if (campaign.getStatus() != CampaignStatus.ACTIVE) {
//            throw new RuntimeException("Cette campagne n'est plus active");
//        }
//        
//        BigDecimal amount = request.getAmount();
//        BigDecimal platformFee = amount.multiply(platformFeePct);
//        BigDecimal processingFee = calculateProcessingFee(amount, request.getMethod());
//        BigDecimal netAmount = amount.subtract(platformFee).subtract(processingFee);
//        
//        Donation donation = new Donation();
//        donation.setAmount(amount);
//        donation.setCampaign(campaign);
//        donation.setDonor(donor);
//        donation.setMessage(request.getMessage());
//        donationRepository.save(donation);
//        
//        Payment payment = new Payment();
//        payment.setTransactionId(UUID.randomUUID().toString());
//        payment.setAmount(amount);
//        payment.setPlatformFee(platformFee);
//        payment.setProcessingFee(processingFee);
//        payment.setNetAmount(netAmount);
//        payment.setMethod(request.getMethod());
//        payment.setStatus(PaymentStatus.PENDING);
//        payment.setDonation(donation);
//        
//        PaymentIntent intent;
//        switch (request.getMethod()) {
//            case STRIPE:
//                intent = stripeProvider.createIntent(payment, campaign, donor);
//                break;
//            case MONCASH:
//                intent = moncashProvider.createIntent(payment, campaign, donor);
//                break;
//            default:
//                throw new IllegalArgumentException("Méthode non supportée");
//        }
//        
//        payment.setProviderResponse(intent.getProviderJson());
//        paymentRepository.save(payment);
//        
//        return intent;
//    }
//    
//    @Transactional
//    public void confirmPayment(String transactionId, String providerResponse) {
//        Payment payment = paymentRepository.findByTransactionId(transactionId)
//            .orElseThrow(() -> new RuntimeException("Transaction non trouvée"));
//            
//        if (payment.getStatus() == PaymentStatus.COMPLETED) return;
//        
//        payment.setStatus(PaymentStatus.COMPLETED);
//        payment.setProviderResponse(providerResponse);
//        
//        Campaign campaign = payment.getDonation().getCampaign();
//        campaign.setCurrentAmount(campaign.getCurrentAmount().add(payment.getAmount()));
//        
//        if (campaign.getCurrentAmount().compareTo(campaign.getGoalAmount()) >= 0) {
//            campaign.setStatus(CampaignStatus.COMPLETED);
//        }
//        
//        campaignRepository.save(campaign);
//        paymentRepository.save(payment);
//    }
//    
//    private BigDecimal calculateProcessingFee(BigDecimal amount, PaymentMethod method) {
//        switch (method) {
//            case STRIPE:
//                return amount.multiply(new BigDecimal("0.029")).add(new BigDecimal("0.30"));
//            case MONCASH:
//                return amount.multiply(new BigDecimal("0.02"));
//            default:
//                return BigDecimal.ZERO;
//        }
//    }
//}
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        
        // 1. Créer et sauvegarder le don
        Donation donation = new Donation();
        donation.setAmount(amount);
        donation.setCampaign(campaign);
        donation.setDonor(donor);
        donation.setMessage(request.getMessage());
        Donation savedDonation = donationRepository.save(donation);
        System.out.println("Donation sauvegardée ID: " + savedDonation.getId());
        
        // 2. Créer et sauvegarder le paiement
        Payment payment = new Payment();
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setAmount(amount);
        payment.setPlatformFee(platformFee);
        payment.setProcessingFee(processingFee);
        payment.setNetAmount(netAmount);
        payment.setMethod(request.getMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setDonation(savedDonation);
        Payment savedPayment = paymentRepository.save(payment);
        System.out.println("Payment sauvegardé - Transaction ID: " + savedPayment.getTransactionId());
        
        // 3. Créer l'intent chez le provider
        PaymentIntent intent;
        switch (request.getMethod()) {
            case STRIPE:
                intent = stripeProvider.createIntent(savedPayment, campaign, donor);
                break;
            case MONCASH:
                intent = moncashProvider.createIntent(savedPayment, campaign, donor);
                break;
            default:
                throw new IllegalArgumentException("Méthode non supportée");
        }
        
        savedPayment.setProviderResponse(intent.getProviderJson());
        paymentRepository.save(savedPayment);
        
        System.out.println("Intent créé avec succès");
        return intent;
    }
    
    @Transactional
    public void confirmPayment(String transactionId, String providerResponse) {
        System.out.println("=== [PAYMENT] Confirmation paiement ===");
        System.out.println("Transaction ID reçu: " + transactionId);
        
        // Chercher par transactionId OU par orderId (pour MonCash)
        Payment payment = paymentRepository.findByTransactionId(transactionId).orElse(null);
        
        if (payment == null) {
            // Essayer de chercher dans providerResponse
            System.out.println("Transaction non trouvée par ID, recherche dans les paiements récents...");
            List<Payment> recentPayments = paymentRepository.findAll().stream()
                .limit(50)
                .collect(Collectors.toList());
            
            for (Payment p : recentPayments) {
                if (p.getProviderResponse() != null && p.getProviderResponse().contains(transactionId)) {
                    payment = p;
                    System.out.println("Trouvé via providerResponse: " + p.getTransactionId());
                    break;
                }
            }
        }
        
        if (payment == null) {
            System.err.println("Transaction non trouvée: " + transactionId);
            throw new RuntimeException("Transaction non trouvée: " + transactionId);
        }
            
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            System.out.println("Paiement déjà confirmé: " + transactionId);
            return;
        }
        
        // MARQUER COMPLETED
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setProviderResponse(providerResponse);
        paymentRepository.save(payment);
        
        // RÉCUPÉRER LA CAMPAGNE ET METTRE À JOUR LE MONTANT
        Donation donation = payment.getDonation();
        Campaign campaign = donation.getCampaign();
        
        System.out.println("Campagne ID: " + campaign.getId());
        System.out.println("Montant du don: " + payment.getAmount());
        System.out.println("Montant actuel AVANT: " + campaign.getCurrentAmount());
        
        // AJOUTER LE MONTANT
        BigDecimal newAmount = campaign.getCurrentAmount().add(payment.getAmount());
        campaign.setCurrentAmount(newAmount);
        
        System.out.println("Montant actuel APRÈS: " + newAmount);
        
        // VÉRIFIER SI OBJECTIF ATTEINT
        if (campaign.getCurrentAmount().compareTo(campaign.getGoalAmount()) >= 0) {
            campaign.setStatus(CampaignStatus.COMPLETED);
            System.out.println("Objectif atteint !");
        }
        
        campaignRepository.save(campaign);
        
        System.out.println("=== [PAYMENT] Paiement confirmé avec succès ===");
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
    public Payment findByTransactionId(String transactionId) {
    return paymentRepository.findByTransactionId(transactionId).orElse(null);
}
}