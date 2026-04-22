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

import com.google.gson.JsonObject;
import com.securityapp.gofundme.dto.DonationRequest;
import com.securityapp.gofundme.dto.PaymentIntent;
import com.securityapp.gofundme.model.*;
import com.securityapp.gofundme.repositories.CampaignRepository;
import com.securityapp.gofundme.repositories.PaymentRepository;
import com.securityapp.gofundme.repositories.DonationRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {
       
    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private StripePaymentProvider stripeProvider;
    
    @Autowired
    private MonCashApi moncashApi;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private CampaignRepository campaignRepository;
    
    @Autowired
    private UserService userService;
    
    @Value("${platform.fee.percentage:0.05}")
    private BigDecimal platformFeePct;
    
    private static final String MONCASH_REDIRECT_BASE = "https://sandbox.moncashbutton.digicelgroup.com/Moncash-business/Payment/Redirect?token=";
    
    @Transactional
    public PaymentIntent createPaymentIntent(DonationRequest request, User donor, HttpSession session) throws Exception {
        System.out.println("=== CREATE PAYMENT INTENT ===");
        
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
            .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));
        
        if (request.getMethod() == PaymentMethod.MONCASH) {
            String orderId = moncashApi.generateOrderId();
            
            session.setAttribute("pending_payment_orderId", orderId);
            session.setAttribute("pending_payment_campaignId", campaign.getId());
            session.setAttribute("pending_payment_amount", request.getAmount().doubleValue());
            session.setAttribute("pending_payment_donor_email", donor.getEmail());
            session.setAttribute("pending_payment_message", request.getMessage());
            session.setAttribute("pending_payment_anonymous", request.isAnonymous());
            
            System.out.println("Session stockée - OrderId: " + orderId);
            
            com.google.gson.JsonObject paymentResponse = moncashApi.createPayment(orderId, request.getAmount().doubleValue());
            
            String paymentToken = null;
            if (paymentResponse.has("payment_token")) {
                com.google.gson.JsonObject paymentTokenObj = paymentResponse.getAsJsonObject("payment_token");
                if (paymentTokenObj.has("token")) {
                    paymentToken = paymentTokenObj.get("token").getAsString();
                }
            }
            
            if (paymentToken == null && paymentResponse.has("token")) {
                paymentToken = paymentResponse.get("token").getAsString();
            }
            
            if (paymentToken == null) {
                throw new RuntimeException("Impossible de créer le paiement MonCash");
            }
            
            String redirectUrl = MONCASH_REDIRECT_BASE + paymentToken;
            return new PaymentIntent(redirectUrl, orderId, paymentResponse.toString());
            
        } else {
            // Stripe - logique existante
            Donation donation = new Donation();
            donation.setAmount(request.getAmount());
            donation.setCampaign(campaign);
            donation.setDonor(donor);
            donation.setMessage(request.getMessage());
            donation = donationRepository.save(donation);
            
            Payment payment = new Payment();
            payment.setTransactionId(UUID.randomUUID().toString());
            payment.setAmount(request.getAmount());
            payment.setPlatformFee(request.getAmount().multiply(platformFeePct));
            payment.setProcessingFee(calculateProcessingFee(request.getAmount(), request.getMethod()));
            payment.setNetAmount(request.getAmount());
            payment.setMethod(request.getMethod());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setDonation(donation);
            payment = paymentRepository.save(payment);
            
            com.stripe.model.PaymentIntent stripeIntent = stripeProvider.createStripeIntent(payment, campaign, donor);
            return new PaymentIntent(stripeIntent.getClientSecret(), payment.getTransactionId(), stripeIntent.toJson());
        }
    }
    
    @Transactional
    public boolean verifyAndConfirmMonCash(String transactionId, HttpSession session) {
        System.out.println("=== VÉRIFICATION MONCASH ===");
        System.out.println("TransactionId reçu: " + transactionId);
        
        String sessionOrderId = (String) session.getAttribute("pending_payment_orderId");
        Long campaignId = (Long) session.getAttribute("pending_payment_campaignId");
        Double sessionAmount = (Double) session.getAttribute("pending_payment_amount");
        String donorEmail = (String) session.getAttribute("pending_payment_donor_email");
        String message = (String) session.getAttribute("pending_payment_message");
        Boolean anonymous = (Boolean) session.getAttribute("pending_payment_anonymous");
        
        System.out.println("Session OrderId: " + sessionOrderId);
        
        if (sessionOrderId == null || campaignId == null) {
            System.err.println("Session expirée ou invalide");
            return false;
        }
        
        try {
            com.google.gson.JsonObject paymentDetails = moncashApi.retrieveTransactionPayment(transactionId);
            
            int status = paymentDetails.get("status").getAsInt();
            com.google.gson.JsonObject payment = paymentDetails.getAsJsonObject("payment");
            String moncashMessage = payment.get("message").getAsString();
            String moncashOrderId = payment.get("reference").getAsString();
            double moncashAmount = payment.get("cost").getAsDouble();
            
            System.out.println("MonCash Status: " + status);
            System.out.println("MonCash OrderId: " + moncashOrderId);
            
            if (status == 200 && 
                "successful".equals(moncashMessage) && 
                moncashOrderId.equals(sessionOrderId) && 
                Math.abs(moncashAmount - sessionAmount) < 0.01) {
                
                System.out.println("✅ Paiement MonCash vérifié avec succès !");
                
                Campaign campaign = campaignRepository.findById(campaignId)
                    .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));
                
                User donor = userService.findByEmail(donorEmail);
                
                Donation donation = new Donation();
                donation.setAmount(BigDecimal.valueOf(sessionAmount));
                donation.setCampaign(campaign);
                donation.setDonor(donor);
                donation.setMessage(message);
                donation = donationRepository.save(donation);
                
                Payment payment = new Payment();
                payment.setTransactionId(transactionId);
                payment.setAmount(BigDecimal.valueOf(sessionAmount));
                payment.setPlatformFee(BigDecimal.valueOf(sessionAmount).multiply(platformFeePct));
                payment.setProcessingFee(BigDecimal.ZERO);
                payment.setNetAmount(BigDecimal.valueOf(sessionAmount));
                payment.setMethod(PaymentMethod.MONCASH);
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setDonation(donation);
                payment.setProviderResponse(paymentDetails.toString());
                paymentRepository.save(payment);
                
                BigDecimal newAmount = campaign.getCurrentAmount().add(BigDecimal.valueOf(sessionAmount));
                campaign.setCurrentAmount(newAmount);
                System.out.println("Campagne mise à jour: " + campaign.getCurrentAmount());
                
                if (campaign.getCurrentAmount().compareTo(campaign.getGoalAmount()) >= 0) {
                    campaign.setStatus(CampaignStatus.COMPLETED);
                }
                campaignRepository.save(campaign);
                
                session.removeAttribute("pending_payment_orderId");
                session.removeAttribute("pending_payment_campaignId");
                session.removeAttribute("pending_payment_amount");
                session.removeAttribute("pending_payment_donor_email");
                session.removeAttribute("pending_payment_message");
                session.removeAttribute("pending_payment_anonymous");
                
                return true;
            } else {
                System.err.println("❌ Échec vérification MonCash");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Erreur vérification MonCash: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private BigDecimal calculateProcessingFee(BigDecimal amount, PaymentMethod method) {
        switch (method) {
            case STRIPE: return amount.multiply(new BigDecimal("0.029")).add(new BigDecimal("0.30"));
            case MONCASH: return amount.multiply(new BigDecimal("0.02"));
            default: return BigDecimal.ZERO;
        }
    }
}