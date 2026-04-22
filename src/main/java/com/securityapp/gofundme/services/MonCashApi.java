/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

@Component
public class MonCashApi {

    private static final HttpClient client = HttpClient.newHttpClient();

    @Value("${moncash.client.id}")
    private String clientId;

    @Value("${moncash.client.secret}")
    private String clientSecret;

    @Value("${moncash.base.url:https://sandbox.moncashbutton.digicelgroup.com/Api}")
    private String baseUrl;

    /**
     * Obtient un token d'accès OAuth2
     */
    public String getAccessToken() throws Exception {
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String requestBody = "scope=read,write&grant_type=client_credentials";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/oauth/token"))
                .header("Authorization", "Basic " + encodedAuth)
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        return json.get("access_token").getAsString();
    }

    /**
     * Crée un paiement MonCash
     */
    public JsonObject createPayment(String orderId, double amount) throws Exception {
        String token = getAccessToken();

        JsonObject data = new JsonObject();
        data.addProperty("amount", amount);
        data.addProperty("orderId", orderId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/CreatePayment"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(data.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    /**
     * Vérifie une transaction MonCash (CRITIQUE pour la sécurité)
     */
    public JsonObject retrieveTransactionPayment(String transactionId) throws Exception {
        String token = getAccessToken();

        JsonObject data = new JsonObject();
        data.addProperty("transactionId", transactionId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/RetrieveTransactionPayment"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(data.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    /**
     * Génère un orderId unique pour MonCash
     */
    public String generateOrderId() {
        Random random = new Random();
        int number = random.nextInt(90000000) + 10000000;
        return "PAY" + number;
    }
}