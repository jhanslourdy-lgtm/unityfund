package com.securityapp.gofundme.services;

import com.securityapp.gofundme.dto.PaymentIntent;
import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.Payment;
import com.securityapp.gofundme.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class MonCashPaymentProvider {
    
    @Value("${moncash.client.id:}")
    private String moncashClientId;
    
    @Value("${moncash.client.secret:}")
    private String moncashSecret;
    
    // URL exacte de ton exemple moncash.java
    private static final String BASE_URL = "https://sandbox.moncashbutton.digicelgroup.com/Moncash-middleware";
    
    private String accessToken;
    private long tokenExpiry;
    
    /**
     * Récupère le token OAuth2 — identique à ton exemple moncash.java
     */
    private String getAccessToken() {
        // Cache le token 55 minutes
        if (accessToken != null && System.currentTimeMillis() < tokenExpiry) {
            return accessToken;
        }
        
        String auth = moncashClientId + ":" + moncashSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        
        // Body exact de ton exemple : scope=read,write&grant_type=client_credentials
        String requestBody = "scope=read,write&grant_type=client_credentials";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        RestTemplate rest = new RestTemplate();
        
        try {
            ResponseEntity<Map> response = rest.postForEntity(BASE_URL + "/oauth/token", request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                accessToken = (String) response.getBody().get("access_token");
                tokenExpiry = System.currentTimeMillis() + 55 * 60 * 1000; // 55 min
                return accessToken;
            }
        } catch (Exception e) {
            System.err.println("MonCash auth error: " + e.getMessage());
            throw new RuntimeException("Erreur authentification MonCash: " + e.getMessage());
        }
        throw new RuntimeException("Impossible d'obtenir le token MonCash");
    }
    
    /**
     * Crée un paiement — identique à ton exemple moncash.java
     */
    public PaymentIntent createIntent(Payment payment, Campaign campaign, User donor) {
        String token = getAccessToken();
        
        // Body JSON exact de ton exemple : {"amount": amount, "orderId": orderId}
        Map<String, Object> data = new HashMap<>();
        data.put("amount", payment.getAmount().doubleValue());
        data.put("orderId", payment.getTransactionId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);
        RestTemplate rest = new RestTemplate();
        
        try {
            // Endpoint exact de ton exemple : /v1/CreatePayment
            ResponseEntity<Map> response = rest.postForEntity(BASE_URL + "/v1/CreatePayment", request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> resp = response.getBody();
                
                // Extraction du payment_token.token
                String paymentToken = "";
                if (resp.containsKey("payment_token")) {
                    Object pt = resp.get("payment_token");
                    if (pt instanceof Map) {
                        paymentToken = (String) ((Map<?, ?>) pt).get("token");
                    }
                }
                
                return new PaymentIntent(paymentToken, payment.getTransactionId(), resp.toString());
            }
        } catch (Exception e) {
            System.err.println("MonCash create error: " + e.getMessage());
            throw new RuntimeException("Erreur création paiement MonCash: " + e.getMessage());
        }
        throw new RuntimeException("Échec création paiement MonCash");
    }
    
    /**
     * Récupère une transaction — identique à ton exemple moncash.java
     */
    public boolean verifyPayment(String transactionId) {
        try {
            String token = getAccessToken();
            
            Map<String, Object> data = new HashMap<>();
            data.put("transactionId", transactionId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);
            RestTemplate rest = new RestTemplate();
            
            // Endpoint exact de ton exemple : /v1/RetrieveTransactionPayment
            ResponseEntity<Map> response = rest.postForEntity(
                BASE_URL + "/v1/RetrieveTransactionPayment", request, Map.class);
            
            if (response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                return body.containsKey("status") && "SUCCESS".equals(body.get("status"));
            }
        } catch (Exception e) {
            System.err.println("Erreur vérification MonCash: " + e.getMessage());
        }
        return false;
    }
}