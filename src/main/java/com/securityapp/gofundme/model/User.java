/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.securityapp.gofundme.model.Role;
import java.time.LocalDateTime;

/**
 *
 * @author Handy
 */
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // Sera stocké hashé via BCrypt

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Role role; // ex: ROLE_USER, ROLE_ADMIN
    
    @Column(nullable = false)
private boolean emailVerified = false;

private String verificationCode;

private java.time.LocalDateTime verificationCodeExpiry;

@Column(length = 64)
    private String resetToken;
    
    private LocalDateTime resetTokenExpiry;
    // Getters et Setters (ou @Data avec Lombok)

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public void setVerificationCodeExpiry(LocalDateTime verificationCodeExpiry) {
        this.verificationCodeExpiry = verificationCodeExpiry;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public LocalDateTime getVerificationCodeExpiry() {
        return verificationCodeExpiry;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Role getRole() {
        return role;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setRole(Role role) {
        this.role = role;
    }
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }
    @Column(length = 2000)
private String bio;

private String profileImageUrl;
private String phone;
private String website;
private String country; // ou city selon ta préférence

// Getters & Setters
public String getBio() { return bio; }
public void setBio(String bio) { this.bio = bio; }
public String getProfileImageUrl() { return profileImageUrl; }
public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
public String getPhone() { return phone; }
public void setPhone(String phone) { this.phone = phone; }
public String getWebsite() { return website; }
public void setWebsite(String website) { this.website = website; }
public String getCountry() { return country; }
public void setCountry(String country) { this.country = country; }  
}
