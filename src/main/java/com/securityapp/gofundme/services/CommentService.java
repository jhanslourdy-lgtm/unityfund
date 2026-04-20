/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.services;

import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.Comment;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.repositories.CampaignRepository;
import com.securityapp.gofundme.repositories.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Transactional
    public Comment addComment(String content, User user, Long campaignId) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Le commentaire ne peut pas être vide");
        }
        
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));

        Comment comment = new Comment();
        comment.setContent(content.trim());
        comment.setUser(user);
        comment.setCampaign(campaign);
        
        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByCampaign(Long campaignId) {
        return commentRepository.findByCampaignIdOrderByCreatedAtDesc(campaignId);
    }

    @Transactional
    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));
        
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Vous ne pouvez pas supprimer ce commentaire");
        }
        
        commentRepository.delete(comment);
    }
}