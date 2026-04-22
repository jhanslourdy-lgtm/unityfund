package com.securityapp.gofundme.services;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.UUID;

@Component
public class MonCashPaymentProvider {

    @Value("${moncash.client.id:}")
    private String moncashClientId;

    @Value("${moncash.client.secret:}")
    private String moncashSecret;

    @Value("${BASE_URL:http://localhost:8080}")
    private String baseUrl;

    // URLs MonCash Sandbox
    private static final String AUTH_URL = "https://sandbox.moncashbutton.digicelgroup.com/Api/oauth/token";
    private static final String CREATE_PAYMENT_URL = "https://sandbox.moncashbutton.digicelgroup.com/Api/v1/CreatePayment";
    private static final String REDIRECT_BASE_URL = "https://sandbox.moncashbutton.digicelgroup.com/Moncash-business/Payment/Redirect?token=";

    private String cachedToken;
    private long tokenExpiry;

    private String getAccessToken() {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiry) {
            System.out.println("MonCash: token en cache utilisé");
            return cachedToken;
        }

        if (moncashClientId == null || moncashClientId.isBlank() ||
                moncashSecret == null || moncashSecret.isBlank()) {
            throw new RuntimeException("MonCash credentials non configurés");
        }

        System.out.println("MonCash: demande nouveau token...");

        try {
            RestTemplate restTemplate = new RestTemplate();

            String auth = moncashClientId + ":" + moncashSecret;
            String encodedAuth = Base64.getEncoder()
                    .encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedAuth);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String body = "grant_type=client_credentials&scope=read,write";
            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(AUTH_URL, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> resp = response.getBody();
                cachedToken = (String) resp.get("access_token");
                
                Integer expiresIn = (Integer) resp.getOrDefault("expires_in", 3600);
                tokenExpiry = System.currentTimeMillis() + (expiresIn * 1000L) - 300000L;
                
                System.out.println("MonCash: token obtenu");
                return cachedToken;
            }
            
            throw new RuntimeException("Échec auth MonCash: " + response.getStatusCode());

        } catch (Exception e) {
            throw new RuntimeException("Erreur auth MonCash: " + e.getMessage(), e);
        }
    }

    public PaymentIntent createIntent(Payment payment, Campaign campaign, User donor) {
        try {
            String token = getAccessToken();
            
            // orderId max 15 caractères pour MonCash
            String rawId = payment.getTransactionId();
            String orderId = (rawId != null && rawId.length() > 15) 
                    ? rawId.replace("-", "").substring(0, 15)
                    : rawId;

            double amount = payment.getAmount().doubleValue();
            
            // URLs de callback
            String returnUrl = baseUrl + "/api/payments/callback/moncash";
            String cancelUrl = baseUrl + "/payment/failed";
            
            System.out.println("MonCash CreatePayment - Return URL: " + returnUrl);
            System.out.println("MonCash CreatePayment - Order ID: " + orderId);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("amount", amount);
            requestBody.put("orderId", orderId);
            requestBody.put("returnUrl", returnUrl);
            requestBody.put("cancelUrl", cancelUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> response = restTemplate.postForEntity(CREATE_PAYMENT_URL, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> resp = response.getBody();
                
                String paymentToken = extractPaymentToken(resp);
                
                if (paymentToken == null) {
                    throw new RuntimeException("Token de paiement non trouvé");
                }
                
                String redirectUrl = REDIRECT_BASE_URL + paymentToken;
                String providerJson = new ObjectMapper().writeValueAsString(resp);
                
                System.out.println("MonCash Redirect URL: " + redirectUrl);
                
                return new PaymentIntent(redirectUrl, orderId, providerJson);
            }
            
            throw new RuntimeException("Échec création paiement MonCash");

        } catch (Exception e) {
            throw new RuntimeException("Erreur création paiement MonCash: " + e.getMessage(), e);
        }
    }

    private String extractPaymentToken(Map<String, Object> resp) {
        // Format 1: { "payment_token": { "token": "xxx" } }
        if (resp.containsKey("payment_token")) {
            Object pt = resp.get("payment_token");
            if (pt instanceof Map) {
                Object t = ((Map<?, ?>) pt).get("token");
                if (t instanceof String) return (String) t;
            } else if (pt instanceof String) {
                return (String) pt;
            }
        }
        // Format 2: { "token": "xxx" }
        if (resp.containsKey("token") && resp.get("token") instanceof String) {
            return (String) resp.get("token");
        }
        // Format 3: redirect_url déjà complète
        if (resp.containsKey("redirect_url") && resp.get("redirect_url") instanceof String) {
            return (String) resp.get("redirect_url");
        }
        return null;
    }
}