package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.Campaign;
import com.securityapp.gofundme.model.CampaignStatus;
import com.securityapp.gofundme.model.Category;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.repositories.CampaignRepository;
import com.securityapp.gofundme.repositories.CategoryRepository;
import com.securityapp.gofundme.services.CampaignLikeService;
import com.securityapp.gofundme.services.CampaignService;
import com.securityapp.gofundme.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class HomeController {

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private CampaignLikeService likeService;

    @Autowired
    private UserService userService;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Taille par défaut des pages
    private static final int PAGE_SIZE = 9;

    @GetMapping({"/", "/home"})
    public String home(Model model, 
                       @AuthenticationPrincipal UserDetails userDetails,
                       @RequestParam(defaultValue = "0") int page) {
        
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending());
        Page<Campaign> campaignPage = campaignRepository.findByStatus(CampaignStatus.ACTIVE, pageable);
        
        model.addAttribute("campaigns", campaignPage.getContent());
        model.addAttribute("campaignPage", campaignPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", campaignPage.getTotalPages());
        
        addLikeDataToModel(model, campaignPage.getContent(), userDetails);
        
        return "index";
    }

//    @GetMapping("/explore")
//    public String explore(@RequestParam(required = false) String q,
//                          @RequestParam(required = false) String category,
//                          @RequestParam(defaultValue = "0") int page,
//                          Model model,
//                          @AuthenticationPrincipal UserDetails userDetails) {
//
//        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending());
//        Page<Campaign> campaignPage;
//
//        if (q != null && !q.trim().isEmpty()) {
//            // Recherche paginée par mot-clé
//            campaignPage = campaignRepository.findByTitleContainingIgnoreCaseAndStatus(
//                q.trim(), CampaignStatus.ACTIVE, pageable);
//            model.addAttribute("searchQuery", q);
//            
//        } else if (category != null && !category.trim().isEmpty()) {
//            // Filtrage paginé par catégorie
//            campaignPage = campaignRepository.findByCategory_NameIgnoreCaseAndStatus(
//                category.trim(), CampaignStatus.ACTIVE, pageable);
//            model.addAttribute("activeCategory", category);
//            
//        } else {
//            // Toutes les campagnes actives (paginées)
//            campaignPage = campaignRepository.findByStatus(CampaignStatus.ACTIVE, pageable);
//        }
//
//        model.addAttribute("campaigns", campaignPage.getContent());
//        model.addAttribute("campaignPage", campaignPage);
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", campaignPage.getTotalPages());
//        
//        // Générer la liste des numéros de page pour la navigation
//        if (campaignPage.getTotalPages() > 0) {
//            List<Integer> pageNumbers = IntStream.rangeClosed(1, campaignPage.getTotalPages())
//                    .boxed()
//                    .collect(Collectors.toList());
//            model.addAttribute("pageNumbers", pageNumbers);
//        }
//        
//        model.addAttribute("categories", categoryRepository.findAll());
//        addLikeDataToModel(model, campaignPage.getContent(), userDetails);
//
//        return "explore";
//    }
@GetMapping("/explore")
public String explore(@RequestParam(required = false) String q,
                      @RequestParam(required = false) String category,
                      @RequestParam(required = false) String location,
                      @RequestParam(defaultValue = "0") int page,
                      Model model,
                      @AuthenticationPrincipal UserDetails userDetails) {

    Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending());
    Page<Campaign> campaignPage;

    String qTrim = (q != null) ? q.trim() : null;
    String catTrim = (category != null) ? category.trim() : null;
    String locTrim = (location != null) ? location.trim() : null;

    boolean hasQ = qTrim != null && !qTrim.isEmpty();
    boolean hasCat = catTrim != null && !catTrim.isEmpty();
    boolean hasLoc = locTrim != null && !locTrim.isEmpty();

    if (hasQ || hasCat || hasLoc) {
        campaignPage = campaignRepository.searchActive(
            CampaignStatus.ACTIVE,
            hasQ ? qTrim : null,
            hasCat ? catTrim : null,
            hasLoc ? locTrim : null,
            pageable
        );
    } else {
        campaignPage = campaignRepository.findByStatus(CampaignStatus.ACTIVE, pageable);
    }

    if (hasQ) model.addAttribute("searchQuery", qTrim);
    if (hasCat) model.addAttribute("activeCategory", catTrim);
    if (hasLoc) model.addAttribute("activeLocation", locTrim);

    model.addAttribute("campaigns", campaignPage.getContent());
    model.addAttribute("campaignPage", campaignPage);
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", campaignPage.getTotalPages());
    
    if (campaignPage.getTotalPages() > 0) {
        List<Integer> pageNumbers = IntStream.rangeClosed(1, campaignPage.getTotalPages())
                .boxed().collect(Collectors.toList());
        model.addAttribute("pageNumbers", pageNumbers);
    }
    
    model.addAttribute("categories", categoryRepository.findAll());
    model.addAttribute("locations", campaignRepository.findDistinctLocationsByStatus(CampaignStatus.ACTIVE));
    
    addLikeDataToModel(model, campaignPage.getContent(), userDetails);
    return "explore";
}
    private void addLikeDataToModel(Model model, List<Campaign> campaigns, UserDetails userDetails) {
        Map<Long, Long> likeCounts = new HashMap<>();
        Map<Long, Boolean> userLikes = new HashMap<>();

        for (Campaign c : campaigns) {
            Long id = c.getId();
            likeCounts.put(id, likeService.countLikes(id));
        }

        if (userDetails != null) {
            try {
                User user = userService.findByEmail(userDetails.getUsername());
                for (Campaign c : campaigns) {
                    Long id = c.getId();
                    userLikes.put(id, likeService.isLikedByUser(user, id));
                }
            } catch (Exception ignored) {}
        }

        model.addAttribute("likeCounts", likeCounts);
        model.addAttribute("userLikes", userLikes);
    }
}