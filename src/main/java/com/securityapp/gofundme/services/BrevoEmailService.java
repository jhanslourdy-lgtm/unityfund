package com.securityapp.gofundme.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class BrevoEmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${app.base.url}")
    private String baseUrl;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from.name}")
    private String fromName;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendVerificationEmail(String toEmail, String token) {

        String url = "https://api.brevo.com/v3/smtp/email";
        String link = baseUrl + "/verify?token=" + token;

        Map<String, Object> body = new HashMap<>();

        Map<String, String> sender = new HashMap<>();
        sender.put("name", fromName);
        sender.put("email", fromEmail);

        Map<String, String> to = new HashMap<>();
        to.put("email", toEmail);

        body.put("sender", sender);
        body.put("to", List.of(to));
        body.put("subject", "Activation de ton compte UnityFund");

        body.put("htmlContent",
                "<div style='font-family:sans-serif'>" +
                "<h2>Bienvenue sur UnityFund</h2>" +
                "<p>Active ton compte en cliquant ci-dessous :</p>" +
                "<a href='" + link + "' style='padding:12px 20px;background:#16a34a;color:white;text-decoration:none;border-radius:6px;'>Activer mon compte</a>" +
                "<p>Si tu n'es pas à l'origine de cette inscription, ignore cet email.</p>" +
                "</div>"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
            System.out.println("✅ Email envoyé via Brevo à " + toEmail);
        } catch (Exception e) {
            System.out.println("❌ Erreur Brevo: " + e.getMessage());
        }
    }
}