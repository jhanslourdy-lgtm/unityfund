/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.dto;

import java.time.LocalDateTime;

public class CommentResponse {
    private Long id;
    private String content;
    private UserSummary user;
    private LocalDateTime createdAt;

    public CommentResponse(Long id, String content, UserSummary user, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.user = user;
        this.createdAt = createdAt;
    }

    public static class UserSummary {
        private String firstName;
        private String lastName;
        private String profileImageUrl;

        public UserSummary(String firstName, String lastName, String profileImageUrl) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.profileImageUrl = profileImageUrl;
        }

        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getProfileImageUrl() { return profileImageUrl; }
    }

    public Long getId() { return id; }
    public String getContent() { return content; }
    public UserSummary getUser() { return user; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}