/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.model;

import jakarta.persistence.*;

@Entity
@Table(name = "reports", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"campaign_id", "reporter_id"})
})
public class Report extends BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ReportReason reason;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.PENDING;

    public enum ReportReason {
        SPAM("Spam ou publicité"),
        FRAUD("Fraude ou escroquerie"),
        INAPPROPRIATE("Contenu inapproprié"),
        MISLEADING("Informations trompeuses"),
        OTHER("Autre");

        private final String label;
        ReportReason(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public enum ReportStatus {
        PENDING("En attente"),
        REVIEWED("En cours d'examen"),
        RESOLVED("Résolu"),
        DISMISSED("Rejeté");

        private final String label;
        ReportStatus(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Campaign getCampaign() { return campaign; }
    public void setCampaign(Campaign campaign) { this.campaign = campaign; }
    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }
    public ReportReason getReason() { return reason; }
    public void setReason(ReportReason reason) { this.reason = reason; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
}