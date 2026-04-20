// Payment.java - Nouvelle entité pour tracer les transactions
package com.securityapp.gofundme.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment extends BaseAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String transactionId; // UUID interne
    
    @ManyToOne
    @JoinColumn(name = "donation_id", nullable = false)
    private Donation donation;
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod method; // STRIPE, MONCASH
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // PENDING, COMPLETED, FAILED, REFUNDED
    
    private BigDecimal amount; // Montant brut
    private BigDecimal platformFee; // Frais UnityFund (ex: 5%)
    private BigDecimal processingFee; // Frais Stripe/MonCash
    private BigDecimal netAmount; // Montant pour le créateur
    
    @Column(length = 1000)
    private String providerResponse; // JSON réponse API
    
    private String stripePaymentIntentId; // Si Stripe
    private String moncashTransactionId; // Si MonCash

    public void setId(Long id) {
        this.id = id;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setDonation(Donation donation) {
        this.donation = donation;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setPlatformFee(BigDecimal platformFee) {
        this.platformFee = platformFee;
    }

    public void setProcessingFee(BigDecimal processingFee) {
        this.processingFee = processingFee;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public void setProviderResponse(String providerResponse) {
        this.providerResponse = providerResponse;
    }

    public void setStripePaymentIntentId(String stripePaymentIntentId) {
        this.stripePaymentIntentId = stripePaymentIntentId;
    }

    public void setMoncashTransactionId(String moncashTransactionId) {
        this.moncashTransactionId = moncashTransactionId;
    }

    public Long getId() {
        return id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Donation getDonation() {
        return donation;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getPlatformFee() {
        return platformFee;
    }

    public BigDecimal getProcessingFee() {
        return processingFee;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public String getProviderResponse() {
        return providerResponse;
    }

    public String getStripePaymentIntentId() {
        return stripePaymentIntentId;
    }

    public String getMoncashTransactionId() {
        return moncashTransactionId;
    }
    
    
}

