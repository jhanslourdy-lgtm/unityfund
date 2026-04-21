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
    
    private static final String BASE_URL = "https://sandbox.moncashbutton.digicelgroup.com/Moncash-middleware";
    
    private String cachedToken;
    private long tokenExpiry;
    
    private String getAccessToken() {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiry) {
            return cachedToken;
        }
        
        // DEBUG : Log les premiers caractères (ne log jamais le secret complet en prod)
        System.out.println("MonCash Debug - Client ID: " + moncashClientId.substring(0, Math.min(8, moncashClientId.length())) + "...");
        System.out.println("MonCash Debug - Secret length: " + moncashSecret.length());
        
        String auth = moncashClientId + ":" + moncashSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        
        // Essai SANS scope d'abord (certains environnements MonCash le rejettent)
        String requestBody = "grant_type=client_credentials";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        RestTemplate rest = new RestTemplate();
        
        try {
            ResponseEntity<Map> response = rest.postForEntity(BASE_URL + "/oauth/token", request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                cachedToken = (String) response.getBody().get("access_token");
                tokenExpiry = System.currentTimeMillis() + 3300000; // 55 min
                System.out.println("MonCash Debug - Token obtenu !");
                return cachedToken;
            }
        } catch (Exception e) {
            System.err.println("MonCash Debug - Erreur: " + e.getMessage());
            // Si 403, c'est que les credentials sont mauvais
            if (e.getMessage() != null && e.getMessage().contains("403")) {
                throw new RuntimeException("MonCash 403 : Vérifiez MONCASH_CLIENT_ID et MONCASH_SECRET sur Render. Credentials rejetés.");
            }
            throw new RuntimeException("Erreur auth MonCash: " + e.getMessage());
        }
        throw new RuntimeException("Impossible d'obtenir le token MonCash");
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
            ResponseEntity<Map> response = rest.postForEntity(BASE_URL + "/v1/CreatePayment", request, Map.class);
            
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
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur création paiement MonCash: " + e.getMessage());
        }
        throw new RuntimeException("Échec création paiement");
    }
}