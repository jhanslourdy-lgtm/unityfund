package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.CampaignMedia;
import jakarta.servlet.http.HttpServletRequest;
import com.securityapp.gofundme.model.CampaignStatus;
import com.securityapp.gofundme.model.Category;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.repositories.CampaignMediaRepository;
import com.securityapp.gofundme.repositories.CampaignRepository;
import com.securityapp.gofundme.repositories.CategoryRepository;
import com.securityapp.gofundme.repositories.DonationRepository;
import com.securityapp.gofundme.services.CampaignLikeService;
import com.securityapp.gofundme.services.CampaignService;
import com.securityapp.gofundme.services.FileStorageService;
import com.securityapp.gofundme.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.UUID;
import java.io.File;
import java.util.List;

@Controller
@RequestMapping("/campaign")
public class CampaignController {
    @Autowired
private CampaignMediaRepository mediaRepository;
    
    @Autowired
    private CampaignService campaignService;

    @Autowired
    private UserService userService;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CampaignLikeService likeService;
    
     @Autowired
    private FileStorageService fileStorageService;
     
    @Autowired
    private DonationRepository donationRepository;

    private static final String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/src/main/resources/static/uploads";

    // ==================== CRÉATION ====================

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("campaign", new Campaign());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("isEdit", false);
        return "newfundraiser";
    }

//    @PostMapping("/save")
//public String saveCampaign(@ModelAttribute("campaign") Campaign campaign,
//                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
//                           Principal principal,
//                           RedirectAttributes redirectAttributes) {
//    try {
//        System.out.println("=== DÉBUT CRÉATION CAMPAGNE ===");
//        System.out.println("Titre : " + campaign.getTitle());
//        System.out.println("Fichier reçu : " + (imageFile != null ? imageFile.getOriginalFilename() : "null"));
//        System.out.println("Taille fichier : " + (imageFile != null ? imageFile.getSize() : 0));
//        
//        User currentUser = userService.findByEmail(principal.getName());
//        System.out.println("Utilisateur : " + currentUser.getEmail());
//
//        // Catégorie
//        if (campaign.getCategory() == null || campaign.getCategory().getId() == null) {
//            redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner une catégorie.");
//            return "redirect:/campaign/create";
//        }
//        
//        Category category = categoryRepository.findById(campaign.getCategory().getId())
//                .orElseThrow(() -> new RuntimeException("Catégorie invalide"));
//        campaign.setCategory(category);
//        System.out.println("Catégorie : " + category.getName());
//
//        // Image
//        if (imageFile != null && !imageFile.isEmpty()) {
//            System.out.println("Traitement de l'image...");
//            String imageUrl = saveImage(imageFile);
//            campaign.setImageUrl(imageUrl);
//            System.out.println("URL image : " + imageUrl);
//        } else {
//            System.out.println("Aucune image fournie");
//        }
//
//        campaignService.save(campaign, currentUser);
//        System.out.println("=== CAMPAGNE SAUVEGARDÉE ===");
//        
//        redirectAttributes.addFlashAttribute("success", "Campagne créée avec succès !");
//        return "redirect:/dashboard";
//        
//    } catch (Exception e) {
//        System.err.println("=== ERREUR CRÉATION ===");
//        e.printStackTrace();
//        redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
//        return "redirect:/campaign/create";
//    }
//}
@PostMapping("/save")
public String saveCampaign(@ModelAttribute("campaign") Campaign campaign,
                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                           @RequestParam(value = "mediaFiles", required = false) MultipartFile[] mediaFiles,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
    try {
        User currentUser = userService.findByEmail(principal.getName());

        if (campaign.getCategory() == null || campaign.getCategory().getId() == null) {
            redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner une catégorie.");
            return "redirect:/campaign/create";
        }
        
        Category category = categoryRepository.findById(campaign.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Catégorie invalide"));
        campaign.setCategory(category);

        // Image principale
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = fileStorageService.saveCampaignImage(imageFile);
            campaign.setImageUrl(imageUrl);
        }

        campaignService.save(campaign, currentUser);

        // Médias additionnels
        if (mediaFiles != null && mediaFiles.length > 0) {
            saveMedias(campaign, mediaFiles);
        }

        redirectAttributes.addFlashAttribute("success", "Campagne créée avec succès !");
        return "redirect:/dashboard";
        
    } catch (Exception e) {
        e.printStackTrace();
        redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
        return "redirect:/campaign/create";
    }
}
    // ==================== ÉDITION ====================

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Principal principal) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));

        User currentUser = userService.findByEmail(principal.getName());
        
        // Sécurité : vérifier que l'utilisateur est le propriétaire
        if (!campaign.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cette campagne.");
        }

        model.addAttribute("campaign", campaign);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("isEdit", true);
        return "newfundraiser";
    }
//
//    @PostMapping("/update/{id}")
//    public String updateCampaign(@PathVariable Long id,
//                                 @ModelAttribute("campaign") Campaign campaignForm,
//                                 @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
//                                 Principal principal,
//                                 RedirectAttributes redirectAttributes) {
//        try {
//            Campaign existingCampaign = campaignRepository.findById(id)
//                    .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));
//
//            User currentUser = userService.findByEmail(principal.getName());
//            
//            // Vérification des droits
//            if (!existingCampaign.getUser().getId().equals(currentUser.getId())) {
//                throw new RuntimeException("Action non autorisée.");
//            }
//
//            // Mise à jour des champs
//            existingCampaign.setTitle(campaignForm.getTitle());
//            existingCampaign.setDescription(campaignForm.getDescription());
//            existingCampaign.setGoalAmount(campaignForm.getGoalAmount());
//            existingCampaign.setLocation(campaignForm.getLocation());
//            existingCampaign.setDurationDays(campaignForm.getDurationDays());
//
//            // Mise à jour catégorie
//            if (campaignForm.getCategory() != null && campaignForm.getCategory().getId() != null) {
//                Category category = categoryRepository.findById(campaignForm.getCategory().getId())
//                        .orElseThrow(() -> new RuntimeException("Catégorie invalide"));
//                existingCampaign.setCategory(category);
//            }
//
//            // Mise à jour image seulement si une nouvelle est fournie
//            if (imageFile != null && !imageFile.isEmpty()) {
//                String imageUrl = saveImage(imageFile);
//                existingCampaign.setImageUrl(imageUrl);
//            }
//
//            campaignRepository.save(existingCampaign);
//            redirectAttributes.addFlashAttribute("success", "Campagne mise à jour avec succès !");
//            return "redirect:/dashboard";
//
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour : " + e.getMessage());
//            return "redirect:/campaign/edit/" + id;
//        }
//    }
@PostMapping("/update/{id}")
public String updateCampaign(@PathVariable Long id,
                             @ModelAttribute("campaign") Campaign campaignForm,
                             @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                             @RequestParam(value = "mediaFiles", required = false) MultipartFile[] mediaFiles,
                             @RequestParam(value = "deleteMediaIds", required = false) List<Long> deleteMediaIds,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
    try {
        Campaign existing = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));

        User currentUser = userService.findByEmail(principal.getName());
        if (!existing.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Action non autorisée.");
        }

        existing.setTitle(campaignForm.getTitle());
        existing.setDescription(campaignForm.getDescription());
        existing.setGoalAmount(campaignForm.getGoalAmount());
        existing.setLocation(campaignForm.getLocation());
        existing.setDurationDays(campaignForm.getDurationDays());

        if (campaignForm.getCategory() != null && campaignForm.getCategory().getId() != null) {
            Category category = categoryRepository.findById(campaignForm.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Catégorie invalide"));
            existing.setCategory(category);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = fileStorageService.saveCampaignImage(imageFile);
            existing.setImageUrl(imageUrl);
        }

        // Suppression médias sélectionnés
        if (deleteMediaIds != null && !deleteMediaIds.isEmpty()) {
            mediaRepository.deleteAllById(deleteMediaIds);
        }

        // Ajout nouveaux médias
        if (mediaFiles != null && mediaFiles.length > 0) {
            saveMedias(existing, mediaFiles);
        }

        campaignRepository.save(existing);
        redirectAttributes.addFlashAttribute("success", "Campagne mise à jour !");
        return "redirect:/dashboard";

    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
        return "redirect:/campaign/edit/" + id;
    }
}
    // ==================== SUPPRESSION ====================

    @GetMapping("/delete/{id}")
    public String deleteCampaign(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            Campaign campaign = campaignRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));

            User currentUser = userService.findByEmail(principal.getName());
            
            if (!campaign.getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Action non autorisée.");
            }

            // Suppression logique (plus sûr que physique)
            campaign.setStatus(CampaignStatus.DELETED);
            campaignRepository.save(campaign);
            
            redirectAttributes.addFlashAttribute("success", "Campagne supprimée.");
            return "redirect:/dashboard";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression : " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    // ==================== DÉTAILS (existant, corrigé) ====================

    @GetMapping("/details/{id}")
public String campaignDetails(@PathVariable Long id, Model model, 
                              @AuthenticationPrincipal UserDetails userDetails,
                              HttpServletRequest request) {
    
    Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));
    
    // ========== OPEN GRAPH META DONNÉES ==========
    String baseUrl = request.getScheme() + "://" + request.getServerName();
    int port = request.getServerPort();
    if (port != 80 && port != 443) {
        baseUrl += ":" + port;
    }
    
    String ogUrl = baseUrl + "/campaign/details/" + id;
    String ogImage = campaign.getImageUrl() != null 
        ? campaign.getImageUrl() 
        : baseUrl + "/images/default-og.jpg";
    
    // Nettoie le HTML et tronque la description
    String rawDesc = campaign.getDescription() != null 
        ? campaign.getDescription().replaceAll("<[^>]*>", "") 
        : "";
    String ogDesc = rawDesc.length() > 200 
        ? rawDesc.substring(0, 200).trim() + "..." 
        : rawDesc;
    if (ogDesc.isBlank()) ogDesc = "Soutenez cette campagne sur UnityFund";
    
    model.addAttribute("ogTitle", campaign.getTitle());
    model.addAttribute("ogDescription", ogDesc);
    model.addAttribute("ogImage", ogImage);
    model.addAttribute("ogUrl", ogUrl);
    // ==============================================
    
    model.addAttribute("campaign", campaign);
    model.addAttribute("donations", donationRepository.findByCampaignIdOrderByCreatedAtDesc(id));
    
    if (userDetails != null) {
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("isLiked", likeService.isLikedByUser(user, id));
        } catch (Exception e) {
            model.addAttribute("isLiked", false);
        }
    } else {
        model.addAttribute("isLiked", false);
    }
    model.addAttribute("likeCount", likeService.countLikes(id));
    
    return "campaign-details";
}
//    @GetMapping("/details/{id}")
//    public String campaignDetails(@PathVariable Long id, Model model, 
//                                  @AuthenticationPrincipal UserDetails userDetails) {
//        Campaign campaign = campaignRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));
//        
//        model.addAttribute("campaign", campaign);
//        model.addAttribute("donations", donationRepository.findByCampaignIdOrderByCreatedAtDesc(id));
//        
//        if (userDetails != null) {
//            try {
//                User user = userService.findByEmail(userDetails.getUsername());
//                model.addAttribute("isLiked", likeService.isLikedByUser(user, id));
//            } catch (Exception e) {
//                model.addAttribute("isLiked", false);
//            }
//        } else {
//            model.addAttribute("isLiked", false);
//        }
//        model.addAttribute("likeCount", likeService.countLikes(id));
//        
//        return "campaign-details";
//    }

     private String saveImage(MultipartFile imageFile) throws IOException {
        return fileStorageService.saveCampaignImage(imageFile);
    }
     private void saveMedias(Campaign campaign, MultipartFile[] mediaFiles) throws IOException {
    int order = mediaRepository.findByCampaignIdOrderByDisplayOrderAsc(campaign.getId()).size();
    for (MultipartFile file : mediaFiles) {
        if (!file.isEmpty()) {
            String url = fileStorageService.saveCampaignImage(file); // Réutilise la même méthode
            CampaignMedia media = new CampaignMedia();
            media.setCampaign(campaign);
            media.setMediaUrl(url);
            media.setType(file.getContentType() != null && file.getContentType().startsWith("video") 
                ? CampaignMedia.MediaType.VIDEO 
                : CampaignMedia.MediaType.IMAGE);
            media.setDisplayOrder(order++);
            mediaRepository.save(media);
        }
    }
}
}