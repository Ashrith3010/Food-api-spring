package com.food_api.food_api.controller;

import com.food_api.food_api.dto.DonationDTO;
import com.food_api.food_api.entity.User;
import com.food_api.food_api.service.DonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/donations")
public class DonationController {
    @Autowired
    private DonationService donationService;

    @PostMapping
    public ResponseEntity<?> createDonation(
            @RequestBody DonationDTO donationDTO,
            @AuthenticationPrincipal User currentUser) {

        if (!List.of("donor", "ngo").contains(currentUser.getType())) {
            return ResponseEntity.status(403)
                    .body(Map.of("success", false,
                            "message", "Only donors and NGOs can create donations"));
        }

        try {
            DonationDTO createdDonation = donationService.createDonation(donationDTO, currentUser);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Donation created successfully",
                    "donation", createdDonation
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false,
                            "message", "Server error",
                            "error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getDonations(
            @RequestParam(required = false) String viewType,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @AuthenticationPrincipal User currentUser) {

        try {
            List<DonationDTO> donations = donationService.getDonations(
                    viewType, city, date, status, sortBy, currentUser);

            return ResponseEntity.ok(Map.of(
                    "donations", donations,
                    "metadata", Map.of("total", donations.size())
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("success", false,
                            "message", "Server error",
                            "error", e.getMessage()));
        }
    }
}