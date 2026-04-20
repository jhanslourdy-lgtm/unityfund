/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.dto;

import java.math.BigDecimal;

public class CreatorBalance {
    private BigDecimal totalRaised;
    private BigDecimal totalFees;
    private BigDecimal available;

    public CreatorBalance(BigDecimal totalRaised, BigDecimal totalFees, BigDecimal available) {
        this.totalRaised = totalRaised;
        this.totalFees = totalFees;
        this.available = available;
    }

    public BigDecimal getTotalRaised() { return totalRaised; }
    public void setTotalRaised(BigDecimal totalRaised) { this.totalRaised = totalRaised; }
    public BigDecimal getTotalFees() { return totalFees; }
    public void setTotalFees(BigDecimal totalFees) { this.totalFees = totalFees; }
    public BigDecimal getAvailable() { return available; }
    public void setAvailable(BigDecimal available) { this.available = available; }
}
