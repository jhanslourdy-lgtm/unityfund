package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.CampaignMedia;
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
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
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

            boolean hasMainImage = imageFile != null && !imageFile.isEmpty();
            boolean hasGallery = hasFiles(mediaFiles);

            if (!hasMainImage && !hasGallery) {
                redirectAttributes.addFlashAttribute("error", "Veuillez ajouter au moins une image principale ou une image dans la galerie.");
                return "redirect:/campaign/create";
            }

            if (hasMainImage) {
                String imageUrl = fileStorageService.saveCampaignImage(imageFile);
                campaign.setImageUrl(imageUrl);
            }

            campaignService.save(campaign, currentUser);

            // Si l'utilisateur ne choisit pas d'image principale, la première image de la galerie devient la couverture.
            if (hasGallery) {
                String firstImageUrl = saveMedias(campaign, mediaFiles);
                if (!hasMainImage && firstImageUrl != null) {
                    campaign.setImageUrl(firstImageUrl);
                    campaignRepository.save(campaign);
                }
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

            if (deleteMediaIds != null && !deleteMediaIds.isEmpty()) {
                mediaRepository.deleteAllById(deleteMediaIds);
            }

            if (hasFiles(mediaFiles)) {
                String firstImageUrl = saveMedias(existing, mediaFiles);
                if (existing.getImageUrl() == null && firstImageUrl != null) {
                    existing.setImageUrl(firstImageUrl);
                }
            }

            campaignRepository.save(existing);
            redirectAttributes.addFlashAttribute("success", "Campagne mise à jour !");
            return "redirect:/dashboard";

        } catch (Exception e) {
            e.printStackTrace();
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

            campaign.setStatus(CampaignStatus.DELETED);
            campaignRepository.save(campaign);

            redirectAttributes.addFlashAttribute("success", "Campagne supprimée.");
            return "redirect:/dashboard";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression : " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    // ==================== DÉTAILS ====================

    @GetMapping("/details/{id}")
    public String campaignDetails(@PathVariable Long id,
                                  Model model,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  HttpServletRequest request) {

        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));

        String baseUrl = request.getScheme() + "://" + request.getServerName();
        int port = request.getServerPort();
        if (port != 80 && port != 443) {
            baseUrl += ":" + port;
        }

        String ogUrl = baseUrl + "/campaign/details/" + id;
        String ogImage = campaign.getImageUrl() != null
                ? campaign.getImageUrl()
                : baseUrl + "/images/default-og.jpg";

        String rawDesc = campaign.getDescription() != null
                ? campaign.getDescription().replaceAll("<[^>]*>", "")
                : "";
        String ogDesc = rawDesc.length() > 200
                ? rawDesc.substring(0, 200).trim() + "..."
                : rawDesc;
        if (ogDesc.isBlank()) {
            ogDesc = "Soutenez cette campagne sur UnityFund";
        }

        model.addAttribute("ogTitle", campaign.getTitle());
        model.addAttribute("ogDescription", ogDesc);
        model.addAttribute("ogImage", ogImage);
        model.addAttribute("ogUrl", ogUrl);
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

    private boolean hasFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return false;
        }
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Enregistre les médias et retourne l'URL de la première image trouvée.
     * Cette URL peut servir de couverture quand aucune image principale n'a été fournie.
     */
    private String saveMedias(Campaign campaign, MultipartFile[] mediaFiles) throws IOException {
        int order = mediaRepository.findByCampaignIdOrderByDisplayOrderAsc(campaign.getId()).size();
        String firstImageUrl = null;

        for (MultipartFile file : mediaFiles) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String contentType = file.getContentType() != null ? file.getContentType() : "";
            boolean isVideo = contentType.startsWith("video/");
            boolean isImage = contentType.startsWith("image/");

            if (!isImage && !isVideo) {
                throw new IllegalArgumentException("Type de fichier non accepté : " + file.getOriginalFilename());
            }

            String url = fileStorageService.saveCampaignMedia(file);

            CampaignMedia media = new CampaignMedia();
            media.setCampaign(campaign);
            media.setMediaUrl(url);
            media.setType(isVideo ? CampaignMedia.MediaType.VIDEO : CampaignMedia.MediaType.IMAGE);
            media.setDisplayOrder(order++);
            mediaRepository.save(media);

            if (isImage && firstImageUrl == null) {
                firstImageUrl = url;
            }
        }

        return firstImageUrl;
    }
}
