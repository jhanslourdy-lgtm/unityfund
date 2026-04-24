/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.model;

public enum WithdrawalStatus {
    PENDING,      // En attente de validation
    PROCESSING,   // En cours de traitement
    COMPLETED,    // Payé
    REJECTED      // Rejeté
}