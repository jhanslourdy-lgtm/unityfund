package com.securityapp.gofundme.controllers;

import com.securityapp.gofundme.model.Donation;
import com.securityapp.gofundme.model.Payment;
import com.securityapp.gofundme.repositories.DonationRepository;
import com.securityapp.gofundme.repositories.PaymentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/admin/donations")
public class AdminDonationController {

    private final DonationRepository donationRepository;
    private final PaymentRepository paymentRepository;

    public AdminDonationController(
            DonationRepository donationRepository,
            PaymentRepository paymentRepository
    ) {
        this.donationRepository = donationRepository;
        this.paymentRepository = paymentRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("donations", donationRepository.findAll());
        return "admin/donations/list";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Don introuvable"));

        Optional<Payment> payment = paymentRepository.findByDonation_Id(donation.getId());

        model.addAttribute("donation", donation);
        model.addAttribute("payment", payment.orElse(null));

        return "admin/donations/view";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Don introuvable"));

        Optional<Payment> payment = paymentRepository.findByDonation_Id(donation.getId());

        payment.ifPresent(paymentRepository::delete);
        donationRepository.delete(donation);

        return "redirect:/admin/donations";
    }
}