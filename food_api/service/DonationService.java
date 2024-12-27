package com.food_api.food_api.service;

import com.food_api.food_api.dto.DonationDTO;
import com.food_api.food_api.entity.Donation;
import com.food_api.food_api.entity.User;
import com.food_api.food_api.repository.DonationRepository;
import com.food_api.food_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DonationService {
    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private UserRepository userRepository;

    public DonationDTO createDonation(DonationDTO donationDTO, User currentUser) {
        Donation donation = new Donation();
        donation.setFoodItem(donationDTO.getFoodItem());
        donation.setQuantity(donationDTO.getQuantity());
        donation.setLocation(donationDTO.getLocation());
        donation.setArea(donationDTO.getArea());
        donation.setExpiryTime(donationDTO.getExpiryTime());
        donation.setServingSize(donationDTO.getServingSize());
        donation.setStorageInstructions(donationDTO.getStorageInstructions());
        donation.setDietaryInfo(donationDTO.getDietaryInfo());
        donation.setDonor(currentUser);
        donation.setClaimed(false);
        donation.setCreatedAt(LocalDateTime.now());
        donation.setUpdatedAt(LocalDateTime.now());

        Donation savedDonation = donationRepository.save(donation);
        return convertToDTO(savedDonation);
    }

    public List<DonationDTO> getDonations(String viewType, String city, LocalDateTime date,
                                          String status, String sortBy, User currentUser) {
        List<Donation> donations = donationRepository.findAll();

        // Apply filters
        if (viewType != null) {
            switch (viewType) {
                case "available":
                    donations = donations.stream()
                            .filter(d -> !d.isClaimed() && d.getExpiryTime().isAfter(LocalDateTime.now()))
                            .collect(Collectors.toList());
                    break;
                case "my-donations":
                    if ("donor".equals(currentUser.getType())) {
                        donations = donations.stream()
                                .filter(d -> d.getDonor().getId().equals(currentUser.getId()))
                                .collect(Collectors.toList());
                    }
                    break;
                case "claimed":
                    donations = donations.stream()
                            .filter(Donation::isClaimed)
                            .collect(Collectors.toList());
                    break;
            }
        }

        // Apply location filter
        if (city != null) {
            final String searchCity = city.toLowerCase();
            donations = donations.stream()
                    .filter(d -> d.getLocation().toLowerCase().equals(searchCity) ||
                            d.getArea().toLowerCase().equals(searchCity))
                    .collect(Collectors.toList());
        }

        // Apply date filter
        if (date != null) {
            LocalDateTime filterDate = date;
            donations = donations.stream()
                    .filter(d -> d.getCreatedAt().toLocalDate().equals(filterDate.toLocalDate()))
                    .collect(Collectors.toList());
        }

        // Apply status filter
        if (status != null) {
            switch (status) {
                case "active":
                    donations = donations.stream()
                            .filter(d -> !d.isClaimed() && d.getExpiryTime().isAfter(LocalDateTime.now()))
                            .collect(Collectors.toList());
                    break;
                case "expired":
                    donations = donations.stream()
                            .filter(d -> d.getExpiryTime().isBefore(LocalDateTime.now()))
                            .collect(Collectors.toList());
                    break;
                case "claimed":
                    donations = donations.stream()
                            .filter(Donation::isClaimed)
                            .collect(Collectors.toList());
                    break;
            }
        }

        // Apply sorting
        if (sortBy != null) {
            switch (sortBy) {
                case "date":
                    donations.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
                    break;
                case "expiry":
                    donations.sort((a, b) -> a.getExpiryTime().compareTo(b.getExpiryTime()));
                    break;
                case "quantity":
                    donations.sort((a, b) -> Integer.compare(
                            Integer.parseInt(b.getQuantity()),
                            Integer.parseInt(a.getQuantity())
                    ));
                    break;
            }
        }

        return donations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DonationDTO convertToDTO(Donation donation) {
        DonationDTO dto = new DonationDTO();
        dto.setId(donation.getId().toString());
        dto.setFoodItem(donation.getFoodItem());
        dto.setQuantity(donation.getQuantity());
        dto.setLocation(donation.getLocation());
        dto.setArea(donation.getArea());
        dto.setExpiryTime(donation.getExpiryTime());
        dto.setServingSize(donation.getServingSize());
        dto.setStorageInstructions(donation.getStorageInstructions());
        dto.setDietaryInfo(donation.getDietaryInfo());
        dto.setDonorId(donation.getDonor().getId().toString());
        dto.setDonorName(donation.getDonor().getUsername());
        dto.setDonorType(donation.getDonor().getType());
        dto.setDonorEmail(donation.getDonor().getEmail());
        dto.setDonorPhone(donation.getDonor().getPhone());
        dto.setClaimed(donation.isClaimed());
        if (donation.getClaimedBy() != null) {
            dto.setClaimedBy(donation.getClaimedBy().getId().toString());
        }
        dto.setClaimedAt(donation.getClaimedAt());
        dto.setCreatedAt(donation.getCreatedAt());
        dto.setUpdatedAt(donation.getUpdatedAt());
        return dto;
    }
}
