//package com.securityapp.gofundme.controllers;
//
//import com.securityapp.gofundme.model.Campaign;
//import com.securityapp.gofundme.model.User; // Import manquant
//import com.securityapp.gofundme.repositories.CampaignRepository;
//import com.securityapp.gofundme.repositories.DonationRepository;
//import com.securityapp.gofundme.services.CampaignLikeService;
//import com.securityapp.gofundme.services.CampaignService;
//import com.securityapp.gofundme.services.UserService; // Import manquant
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import java.security.Principal; // Import manquant pour identifier l'utilisateur
//import java.io.IOException;
//import java.nio.file.*;
//import java.util.UUID;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.core.userdetails.UserDetails;
//
//@Controller
//@RequestMapping("/campaign")
//public class CampaignController {
//
//    @Autowired
//    private CampaignService campaignService;
//
//    @Autowired
//    private UserService userService; // Ajout nécessaire pour récupérer l'objet User
//
//    private static String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/src/main/resources/static/uploads";
//
//    @GetMapping("/create")
//    public String showCreateForm(Model model) {
//        model.addAttribute("campaign", new Campaign());
//        return "newfundraiser";
//    }
//
//    @PostMapping("/save")
//    public String saveCampaign(@ModelAttribute("campaign") Campaign campaign, 
//                               @RequestParam("imageFile") MultipartFile imageFile,
//                               Principal principal) { // Ajout de Principal
//        
//        // CORRECTION : Définir la variable 'user'
//        String email = principal.getName();
//        User currentUser = userService.findByEmail(email); 
//        
//        if (!imageFile.isEmpty()) {
//            try {
//                Path uploadPath = Paths.get(UPLOAD_DIRECTORY);
//                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
//
//                String original = imageFile.getOriginalFilename();
//String extension = original.substring(original.lastIndexOf('.'));
//String fileName = UUID.randomUUID() + extension;
//
//                Path filePath = uploadPath.resolve(fileName);
//                Files.copy(imageFile.getInputStream(), filePath);
//
//                campaign.setImageUrl("/uploads/" + fileName);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        
//        // Utiliser la variable définie 'currentUser'
//        campaignService.save(campaign, currentUser); 
//
//        return "redirect:/dashboard";
//    }
//    // DANS CampaignController.java (pas CampaignLikeController)
//@Autowired
//private CampaignLikeService likeService;
//
//@Autowired
//private DonationRepository donationRepository;
//
//@Autowired
//private CampaignRepository campaignRepository;
//
//@GetMapping("/details/{id}")
//public String campaignDetails(@PathVariable Long id, Model model, 
//                              @AuthenticationPrincipal UserDetails userDetails) {
//    Campaign campaign = campaignRepository.findById(id)
//            .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));
//    
//    model.addAttribute("campaign", campaign);
//    model.addAttribute("donations", donationRepository.findByCampaignIdOrderByCreatedAtDesc(id));
//    
//    if (userDetails != null) {
//        try {
//            User user = userService.findByEmail(userDetails.getUsername());
//            model.addAttribute("isLiked", likeService.isLikedByUser(user, id));
//        } catch (Exception e) {
//            model.addAttribute("isLiked", false);
//        }
//    } else {
//        model.addAttribute("isLiked", false);
//    }
//    model.addAttribute("likeCount", likeService.countLikes(id));
//    
//    return "campaign-details";
//}
//}
package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.CampaignStatus;
import com.securityapp.gofundme.model.Category;
import com.securityapp.gofundme.model.User;
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

@Controller
@RequestMapping("/campaign")
public class CampaignController {

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

    @PostMapping("/save")
public String saveCampaign(@ModelAttribute("campaign") Campaign campaign,
                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
    try {
        System.out.println("=== DÉBUT CRÉATION CAMPAGNE ===");
        System.out.println("Titre : " + campaign.getTitle());
        System.out.println("Fichier reçu : " + (imageFile != null ? imageFile.getOriginalFilename() : "null"));
        System.out.println("Taille fichier : " + (imageFile != null ? imageFile.getSize() : 0));
        
        User currentUser = userService.findByEmail(principal.getName());
        System.out.println("Utilisateur : " + currentUser.getEmail());

        // Catégorie
        if (campaign.getCategory() == null || campaign.getCategory().getId() == null) {
            redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner une catégorie.");
            return "redirect:/campaign/create";
        }
        
        Category category = categoryRepository.findById(campaign.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Catégorie invalide"));
        campaign.setCategory(category);
        System.out.println("Catégorie : " + category.getName());

        // Image
        if (imageFile != null && !imageFile.isEmpty()) {
            System.out.println("Traitement de l'image...");
            String imageUrl = saveImage(imageFile);
            campaign.setImageUrl(imageUrl);
            System.out.println("URL image : " + imageUrl);
        } else {
            System.out.println("Aucune image fournie");
        }

        campaignService.save(campaign, currentUser);
        System.out.println("=== CAMPAGNE SAUVEGARDÉE ===");
        
        redirectAttributes.addFlashAttribute("success", "Campagne créée avec succès !");
        return "redirect:/dashboard";
        
    } catch (Exception e) {
        System.err.println("=== ERREUR CRÉATION ===");
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

    @PostMapping("/update/{id}")
    public String updateCampaign(@PathVariable Long id,
                                 @ModelAttribute("campaign") Campaign campaignForm,
                                 @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            Campaign existingCampaign = campaignRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));

            User currentUser = userService.findByEmail(principal.getName());
            
            // Vérification des droits
            if (!existingCampaign.getUser().getId().equals(currentUser.getId())) {
                throw new RuntimeException("Action non autorisée.");
            }

            // Mise à jour des champs
            existingCampaign.setTitle(campaignForm.getTitle());
            existingCampaign.setDescription(campaignForm.getDescription());
            existingCampaign.setGoalAmount(campaignForm.getGoalAmount());
            existingCampaign.setLocation(campaignForm.getLocation());
            existingCampaign.setDurationDays(campaignForm.getDurationDays());

            // Mise à jour catégorie
            if (campaignForm.getCategory() != null && campaignForm.getCategory().getId() != null) {
                Category category = categoryRepository.findById(campaignForm.getCategory().getId())
                        .orElseThrow(() -> new RuntimeException("Catégorie invalide"));
                existingCampaign.setCategory(category);
            }

            // Mise à jour image seulement si une nouvelle est fournie
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = saveImage(imageFile);
                existingCampaign.setImageUrl(imageUrl);
            }

            campaignRepository.save(existingCampaign);
            redirectAttributes.addFlashAttribute("success", "Campagne mise à jour avec succès !");
            return "redirect:/dashboard";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour : " + e.getMessage());
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
                                  @AuthenticationPrincipal UserDetails userDetails) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));
        
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

// private String saveImage(MultipartFile imageFile) throws IOException {
//    String projectDir = System.getProperty("user.dir");
//    
//    // Chemin vers target/classes/static/uploads (dossier servi par Spring Boot)
//    Path targetPath = Paths.get(projectDir, "target", "classes", "static", "uploads");
//    
//    // Chemin vers src/main/resources/static/uploads (pour persistance après rebuild)
//    Path srcPath = Paths.get(projectDir, "src", "main", "resources", "static", "uploads");
//    
//    // Créer les dossiers si nécessaire
//    if (!Files.exists(targetPath)) {
//        Files.createDirectories(targetPath);
//    }
//    if (!Files.exists(srcPath)) {
//        Files.createDirectories(srcPath);
//    }
//
//    String originalFilename = imageFile.getOriginalFilename();
//    String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
//    String fileName = UUID.randomUUID().toString() + extension;
//
//    // Sauvegarder dans target (pour affichage immédiat)
//    Path targetFile = targetPath.resolve(fileName);
//    Files.copy(imageFile.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
//    
//    // Sauvegarder aussi dans src (pour persistance après rebuild)
//    try {
//        Path srcFile = srcPath.resolve(fileName);
//        Files.copy(targetFile, srcFile, StandardCopyOption.REPLACE_EXISTING);
//    } catch (Exception e) {
//        System.err.println("Impossible de copier dans src: " + e.getMessage());
//    }
//    
//    System.out.println("Image sauvegardée: " + targetFile.toAbsolutePath());
//    return "/uploads/" + fileName;
//}
     private String saveImage(MultipartFile imageFile) throws IOException {
        return fileStorageService.saveCampaignImage(imageFile);
    }
}