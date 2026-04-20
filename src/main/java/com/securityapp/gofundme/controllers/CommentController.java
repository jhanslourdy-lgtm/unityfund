/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.dto.CommentResponse;
import com.securityapp.gofundme.model.Comment;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.services.CommentService;
import com.securityapp.gofundme.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/campaigns/{campaignId}/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @GetMapping
public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long campaignId) {
    return ResponseEntity.ok(commentService.getCommentsByCampaign(campaignId));
}

    @PostMapping
    public ResponseEntity<?> addComment(@PathVariable Long campaignId,
                                        @RequestBody Map<String, String> payload,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Connexion requise");
        }
        
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            Comment comment = commentService.addComment(payload.get("content"), user, campaignId);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long campaignId,
                                           @PathVariable Long commentId,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Connexion requise");
        }
        
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            commentService.deleteComment(commentId, user);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}