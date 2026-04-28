package com.securityapp.gofundme.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "donations")
public class Donation extends BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DonationStatus status = DonationStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User donor;

    private String message;

    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public DonationStatus getStatus() {
        return status;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public User getDonor() {
        return donor;
    }

    public String getMessage() {
        return message;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setStatus(DonationStatus status) {
        this.status = status;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public void setDonor(User donor) {
        this.donor = donor;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}