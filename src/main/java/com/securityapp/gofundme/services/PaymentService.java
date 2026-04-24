package com.securityapp.gofundme.services;

import com.securityapp.gofundme.dto.DonationRequest;
import com.securityapp.gofundme.dto.PaymentIntent;
import com.securityapp.gofundme.model.AuditAction;
import com.securityapp.gofundme.model.AuditStatus;
import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.CampaignStatus;
import com.securityapp.gofundme.model.Donation;
import com.securityapp.gofundme.model.Payment;
import com.securityapp.gofundme.model.PaymentMethod;
import com.securityapp.gofundme.model.PaymentStatus;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.repositories.CampaignRepository;
import com.securityapp.gofundme.repositories.DonationRepository;
import com.securityapp.gofundme.repositories.PaymentRepository;
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
    private MonCashPaymentProvider moncashProvider;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Value("${platform.fee.percentage:0.05}")
    private BigDecimal platformFeePct;

    @Transactional
    public PaymentIntent createPaymentIntent(DonationRequest request, User donor) throws Exception {
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));

        auditLogService.system(
                AuditAction.DONATION_CREATE_ATTEMPT,
                AuditStatus.ATTEMPT,
                "Donation",
                null,
                "Tentative de don",
                null,
                "amount=" + request.getAmount()
                        + "; method=" + request.getMethod()
                        + "; campaignId=" + request.getCampaignId()
                        + "; donor=" + safeEmail(donor),
                null
        );

        if (campaign.getStatus() != CampaignStatus.ACTIVE) {
            auditLogService.system(
                    AuditAction.DONATION_PAYMENT_FAILED,
                    AuditStatus.FAILED,
                    "Campaign",
                    campaign.getId(),
                    "Don refusé : campagne inactive",
                    null,
                    "campaignStatus=" + campaign.getStatus()
                            + "; amount=" + request.getAmount()
                            + "; donor=" + safeEmail(donor),
                    null
            );
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

        try {
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

            auditLogService.system(
                    AuditAction.DONATION_PAYMENT_PENDING,
                    AuditStatus.PENDING,
                    "Payment",
                    payment.getId(),
                    "Don créé, paiement en attente",
                    null,
                    "transactionId=" + payment.getTransactionId()
                            + "; donationId=" + donation.getId()
                            + "; amount=" + amount
                            + "; method=" + request.getMethod()
                            + "; campaignId=" + campaign.getId()
                            + "; donor=" + safeEmail(donor),
                    null
            );

            return intent;
        } catch (Exception e) {
            auditLogService.system(
                    AuditAction.DONATION_PAYMENT_FAILED,
                    AuditStatus.FAILED,
                    "Donation",
                    donation.getId(),
                    "Échec pendant la création du paiement",
                    null,
                    "amount=" + amount
                            + "; method=" + request.getMethod()
                            + "; campaignId=" + campaign.getId()
                            + "; donor=" + safeEmail(donor),
                    e.getMessage()
            );
            throw e;
        }
    }

    @Transactional
    public void confirmPayment(String transactionId, String providerResponse) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction non trouvée"));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            auditLogService.system(
                    AuditAction.DONATION_PAYMENT_SUCCESS,
                    AuditStatus.SUCCESS,
                    "Payment",
                    payment.getId(),
                    "Paiement déjà confirmé",
                    "COMPLETED",
                    "COMPLETED; transactionId=" + transactionId,
                    null
            );
            return;
        }

        PaymentStatus oldPaymentStatus = payment.getStatus();
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setProviderResponse(providerResponse);

        Campaign campaign = payment.getDonation().getCampaign();
        BigDecimal oldCampaignAmount = campaign.getCurrentAmount();
        campaign.setCurrentAmount(campaign.getCurrentAmount().add(payment.getAmount()));

        if (campaign.getCurrentAmount().compareTo(campaign.getGoalAmount()) >= 0) {
            campaign.setStatus(CampaignStatus.COMPLETED);
        }

        campaignRepository.save(campaign);
        paymentRepository.save(payment);

        auditLogService.system(
                AuditAction.DONATION_PAYMENT_SUCCESS,
                AuditStatus.SUCCESS,
                "Payment",
                payment.getId(),
                "Paiement confirmé avec succès",
                "status=" + oldPaymentStatus + "; campaignAmount=" + oldCampaignAmount,
                "status=" + payment.getStatus()
                        + "; campaignAmount=" + campaign.getCurrentAmount()
                        + "; transactionId=" + payment.getTransactionId()
                        + "; donationId=" + payment.getDonation().getId()
                        + "; campaignId=" + campaign.getId()
                        + "; amount=" + payment.getAmount()
                        + "; method=" + payment.getMethod(),
                null
        );

        auditLogService.system(
                AuditAction.DONATION_CONFIRMED,
                AuditStatus.SUCCESS,
                "Donation",
                payment.getDonation().getId(),
                "Don confirmé",
                null,
                "amount=" + payment.getAmount()
                        + "; campaign=" + campaign.getTitle()
                        + "; campaignId=" + campaign.getId(),
                null
        );
    }

    @Transactional
    public void failPayment(String transactionId, String providerResponse) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction non trouvée"));

        PaymentStatus oldPaymentStatus = payment.getStatus();
        payment.setStatus(PaymentStatus.FAILED);
        payment.setProviderResponse(providerResponse);
        paymentRepository.save(payment);

        auditLogService.system(
                AuditAction.DONATION_PAYMENT_FAILED,
                AuditStatus.FAILED,
                "Payment",
                payment.getId(),
                "Paiement échoué",
                "status=" + oldPaymentStatus,
                "status=" + payment.getStatus()
                        + "; transactionId=" + payment.getTransactionId()
                        + "; donationId=" + payment.getDonation().getId()
                        + "; campaignId=" + payment.getDonation().getCampaign().getId()
                        + "; amount=" + payment.getAmount()
                        + "; method=" + payment.getMethod(),
                providerResponse
        );
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

    private String safeEmail(User user) {
        return user != null ? user.getEmail() : "ANONYMOUS";
    }
}
