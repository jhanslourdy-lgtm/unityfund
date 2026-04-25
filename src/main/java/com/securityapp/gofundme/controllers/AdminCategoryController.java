package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.AuditAction;
import com.securityapp.gofundme.model.AuditStatus;
import com.securityapp.gofundme.model.Category;
import com.securityapp.gofundme.repositories.CategoryRepository;
import com.securityapp.gofundme.services.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {
    private final CategoryRepository categoryRepository;
    private final AuditLogService auditLogService;

    public AdminCategoryController(CategoryRepository categoryRepository, AuditLogService auditLogService) {
        this.categoryRepository = categoryRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String list(Model model) {
        if (!model.containsAttribute("category")) model.addAttribute("category", new Category());
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/categories/list";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Category category, HttpServletRequest request) {
        boolean update = category.getId() != null;
        Category oldCategory = update ? categoryRepository.findById(category.getId()).orElse(null) : null;
        Category saved = categoryRepository.save(category);
        auditLogService.log(AuditAction.ADMIN_ACTION, AuditStatus.SUCCESS, "Category", saved.getId(),
                update ? "Catégorie modifiée" : "Catégorie créée",
                oldCategory == null ? null : "name=" + oldCategory.getName() + ", description=" + oldCategory.getDescription(),
                "name=" + saved.getName() + ", description=" + saved.getDescription(), null, request);
        return "redirect:/admin/categories";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
        model.addAttribute("category", category);
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/categories/list";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Catégorie introuvable"));
        if (category.getCampaigns() != null && !category.getCampaigns().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Impossible de supprimer cette catégorie : elle contient des campagnes.");
            return "redirect:/admin/categories";
        }
        String oldValue = "name=" + category.getName() + ", description=" + category.getDescription();
        categoryRepository.delete(category);
        auditLogService.log(AuditAction.ADMIN_ACTION, AuditStatus.SUCCESS, "Category", id,
                "Catégorie supprimée", oldValue, null, null, request);
        return "redirect:/admin/categories";
    }
}
