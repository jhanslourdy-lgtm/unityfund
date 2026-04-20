///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package com.securityapp.gofundme.controllers;
//
//import com.securityapp.gofundme.model.User;
//import com.securityapp.gofundme.services.CampaignService;
//import com.securityapp.gofundme.services.FileStorageService;
//import com.securityapp.gofundme.services.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.security.Principal;
//
//@Controller
//public class ProfileController {
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private CampaignService campaignService;
//
//    @Autowired
//    private FileStorageService fileStorageService;
//
//    // ========== PROFIL PUBLIC DU CRÉATEUR ==========
//    @GetMapping("/creator/{userId}")
//    public String viewCreatorProfile(@PathVariable Long userId, Model model) {
//        User creator = userService.findById(userId);
//        model.addAttribute("creator", creator);
//        model.addAttribute("campaigns", campaignService.findByUser(creator));
//        model.addAttribute("totalRaised", campaignService.getTotalRaisedByUser(creator));
//        return "creator-profile";
//    }
//
//    // ========== PARAMÈTRES DE PROFIL (privé) ==========
//    @GetMapping("/profile/settings")
//    public String profileSettings(Model model, Principal principal) {
//        User user = userService.findByEmail(principal.getName());
//        model.addAttribute("user", user);
//        return "profile-settings";
//    }
//
//    @PostMapping("/profile/settings")
//    public String updateProfile(@RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
//                                @RequestParam(required = false) String bio,
//                                @RequestParam(required = false) String phone,
//                                @RequestParam(required = false) String website,
//                                @RequestParam(required = false) String country,
//                                Principal principal,
//                                RedirectAttributes redirectAttributes) {
//        try {
//            User user = userService.findByEmail(principal.getName());
//
//            user.setBio(bio);
//            user.setPhone(phone);
//            user.setWebsite(website);
//            user.setCountry(country);
//
//            if (imageFile != null && !imageFile.isEmpty()) {
//                String imageUrl = fileStorageService.saveProfileImage(imageFile);
//                user.setProfileImageUrl(imageUrl);
//            }
//
//            userService.updateUser(user);
//            redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès !");
//            
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
//        }
//        return "redirect:/profile/settings";
//    }
//}
package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.services.CampaignService;
import com.securityapp.gofundme.services.FileStorageService;
import com.securityapp.gofundme.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private FileStorageService fileStorageService;

    // ========== PROFIL PUBLIC (vu par les donateurs) ==========
    @GetMapping("/creator/{userId}")
    public String viewCreatorProfile(@PathVariable Long userId, Model model) {
        User creator = userService.findById(userId);
        model.addAttribute("creator", creator);
        model.addAttribute("campaigns", campaignService.findByUser(creator));
        model.addAttribute("totalRaised", campaignService.getTotalRaisedByUser(creator));
        return "creator-profile";
    }

    // ========== PAGE PARAMÈTRES (privée) ==========
    @GetMapping("/profile/settings")
    public String profileSettings(Model model, Principal principal) {
        User user = userService.findByEmail(principal.getName());
        model.addAttribute("user", user);
        return "profile-settings";
    }

    @PostMapping("/profile/settings")
    public String updateProfile(@RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                @RequestParam(required = false) String bio,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String website,
                                @RequestParam(required = false) String country,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(principal.getName());

            user.setBio(bio);
            user.setPhone(phone);
            user.setWebsite(website);
            user.setCountry(country);

            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = fileStorageService.saveProfileImage(imageFile);
                user.setProfileImageUrl(imageUrl);
            }

            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès !");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/profile/settings";
    }
}