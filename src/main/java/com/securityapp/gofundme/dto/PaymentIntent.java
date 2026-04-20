/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.dto;

public class PaymentIntent {
    private String clientSecret;
    private String transactionId;
    private String providerJson;

    public PaymentIntent(String clientSecret, String transactionId, String providerJson) {
        this.clientSecret = clientSecret;
        this.transactionId = transactionId;
        this.providerJson = providerJson;
    }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getProviderJson() { return providerJson; }
    public void setProviderJson(String providerJson) { this.providerJson = providerJson; }
}