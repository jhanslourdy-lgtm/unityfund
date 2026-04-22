package com.securityapp.gofundme.services;

import com.securityapp.gofundme.dto.PaymentIntent;
import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.Payment;
import com.securityapp.gofundme.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
    
    @Value("${moncash.base.url:https://sandbox.moncashbutton.digicelgroup.com/Api}")
    private String baseUrl;
    
    private String accessToken;
    private long tokenExpiry;
    
    /**
     * Récupère le token OAuth2
     */
    private String getAccessToken() {
        // Vérification des credentials
        if (moncashClientId == null || moncashClientId.isBlank() || 
            moncashSecret == null || moncashSecret.isBlank()) {
            throw new RuntimeException("MonCash credentials non configurés (moncash.client.id / moncash.client.secret)");
        }
        
        // Cache le token 55 minutes
        if (accessToken != null && System.currentTimeMillis() < tokenExpiry) {
            return accessToken;
        }
        
        String auth = moncashClientId + ":" + moncashSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        // Utilisation de MultiValueMap (plus fiable qu'une String brute)
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("scope", "read,write");
        body.add("grant_type", "client_credentials");
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        RestTemplate rest = new RestTemplate();
        
        try {
            ResponseEntity<Map> response = rest.postForEntity(baseUrl + "/oauth/token", request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                accessToken = (String) response.getBody().get("access_token");
                tokenExpiry = System.currentTimeMillis() + 55 * 60 * 1000;
                return accessToken;
            } else {
                throw new RuntimeException("MonCash auth failed: HTTP " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("MonCash auth error: " + e.getMessage());
            throw new RuntimeException("Erreur authentification MonCash: " + e.getMessage());
        }
    }
    
    public PaymentIntent createIntent(Payment payment, Campaign campaign, User donor) {
        String token = getAccessToken();
        
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
            ResponseEntity<Map> response = rest.postForEntity(baseUrl + "/v1/CreatePayment", request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> resp = response.getBody();
                
                String paymentToken = "";
                if (resp.containsKey("payment_token")) {
                    Object pt = resp.get("payment_token");
                    if (pt instanceof Map) {
                        paymentToken = (String) ((Map<?, ?>) pt).get("token");
                    }
                }
                
                return new PaymentIntent(paymentToken, payment.getTransactionId(), resp.toString());
            } else {
                throw new RuntimeException("MonCash create payment failed: HTTP " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("MonCash create error: " + e.getMessage());
            throw new RuntimeException("Erreur création paiement MonCash: " + e.getMessage());
        }
    }
    
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
            
            ResponseEntity<Map> response = rest.postForEntity(
                baseUrl + "/v1/RetrieveTransactionPayment", request, Map.class);
            
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