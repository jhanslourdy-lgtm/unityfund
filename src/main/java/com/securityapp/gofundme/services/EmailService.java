package com.securityapp.gofundme.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@Service
public class EmailService {

    private final String API_KEY = "TON_API_KEY_BREVO";

    public void sendVerificationEmail(String toEmail, String token) {

        String url = "https://api.brevo.com/v3/smtp/email";

        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> body = new HashMap<>();

        body.put("sender", Map.of(
                "name", "UnityFund",
                "email", "no-reply@unityfund.com"
        ));

        body.put("to", List.of(Map.of("email", toEmail)));

        body.put("subject", "Vérifie ton compte UnityFund");

        String link = "https://ton-site.com/verify?token=" + token;

        body.put("htmlContent",
                "<div style='font-family:sans-serif'>" +
                "<h2>Bienvenue sur UnityFund</h2>" +
                "<p>Clique ici pour activer ton compte :</p>" +
                "<a href='" + link + "' style='background:#16a34a;color:white;padding:10px 15px;text-decoration:none;border-radius:5px;'>Vérifier</a>" +
                "<p>Si ça ne marche pas, copie ce lien :</p>" +
                "<p>" + link + "</p>" +
                "</div>"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            System.out.println("❌ Brevo failed → fallback console");
            System.out.println("Lien verification: " + link);
        }
    }
    public void sendResetPasswordEmail(String toEmail, String token) {

    String url = "https://api.brevo.com/v3/smtp/email";

    RestTemplate restTemplate = new RestTemplate();

    String link = "https://ton-site.com/reset-password?token=" + token;

    Map<String, Object> body = new HashMap<>();

    body.put("sender", Map.of(
            "name", "UnityFund",
            "email", "no-reply@unityfund.com"
    ));

    body.put("to", List.of(Map.of("email", toEmail)));

    body.put("subject", "Réinitialisation de ton mot de passe");

    body.put("htmlContent",
        "<div style='font-family:sans-serif'>" +
        "<h2 style='color:#16a34a;'>UnityFund</h2>" +
        "<p>Tu as demandé à réinitialiser ton mot de passe.</p>" +
        "<a href='" + link + "' style='background:#dc2626;color:white;padding:10px 15px;border-radius:5px;text-decoration:none;'>Réinitialiser</a>" +
        "<p>Ce lien expire dans 1 heure.</p>" +
        "<p>Si ce n’est pas toi, ignore cet email.</p>" +
        "</div>"
    );

    HttpHeaders headers = new HttpHeaders();
    headers.set("api-key", API_KEY);
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

    try {
        restTemplate.postForEntity(url, request, String.class);
    } catch (Exception e) {
        System.out.println("❌ Brevo failed → fallback");
        System.out.println("Lien reset: " + link);
    }
}
}