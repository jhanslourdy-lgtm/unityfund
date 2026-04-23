/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 *
 * @author Handy
 */
@Entity
@Table(name = "campaigns")
public class Campaign extends BaseAudit { // On hérite de l'audit (voir plus bas)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal goalAmount; // Objectif à atteindre

    @Column(nullable = false)
    private BigDecimal currentAmount = BigDecimal.ZERO; // Montant déjà récolté

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private CampaignStatus status = CampaignStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGoalAmount(BigDecimal goalAmount) {
        this.goalAmount = goalAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getGoalAmount() {
        return goalAmount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public Category getCategory() {
        return category;
    }
    // AJOUTE CECI : Le lien vers l'utilisateur
    @ManyToOne
    @JoinColumn(name = "user_id") // La colonne dans la table SQL
    private User user;

    // Assure-toi d'avoir le getter et le setter correspondant
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }
    private String location;

// N'oubliez pas le Getter et le Setter (ou utilisez @Data si vous avez Lombok)
public String getLocation() {
    return location;
}

public void setLocation(String location) {
    this.location = location;
}
private Integer durationDays; // ou int

// Ajoutez le Getter
public Integer getDurationDays() {
    return durationDays;
}

// Ajoutez le Setter
public void setDurationDays(Integer durationDays) {
    this.durationDays = durationDays;
}
public int getPercentage() {
    if (goalAmount == null || goalAmount.compareTo(BigDecimal.ZERO) == 0) return 0;
    return currentAmount.multiply(new BigDecimal(100))
        .divide(goalAmount, 0, RoundingMode.HALF_UP)
        .intValue();
}
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<CampaignMedia> medias;

    public List<CampaignMedia> getMedias() { return medias; }
    public void setMedias(List<CampaignMedia> medias) { this.medias = medias; }
}