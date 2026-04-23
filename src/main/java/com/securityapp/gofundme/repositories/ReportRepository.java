/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.repositories;

import com.securityapp.gofundme.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByCampaignIdAndReporterId(Long campaignId, Long reporterId);
    List<Report> findByCampaignIdOrderByCreatedAtDesc(Long campaignId);
    long countByCampaignId(Long campaignId);
}