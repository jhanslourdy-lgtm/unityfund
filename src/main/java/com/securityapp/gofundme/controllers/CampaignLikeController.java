package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.dto.LikeResponse;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.services.CampaignLikeService;
import com.securityapp.gofundme.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignLikeController {

    @Autowired
    private CampaignLikeService likeService;

    @Autowired
    private UserService userService;

    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(401).body("Connexion requise");
            }
            User user = userService.findByEmail(userDetails.getUsername());
            LikeResponse response = likeService.toggleLike(user, id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/likes")
    public ResponseEntity<Long> getLikeCount(@PathVariable Long id) {
        return ResponseEntity.ok(likeService.countLikes(id));
    }
}