package com.food_api.food_api.service;

import com.food_api.food_api.repository.DonationRepository;
import com.food_api.food_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonationRepository donationRepository;

    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();

        // Count total donors
        stats.put("totalDonors", userRepository.countByType("donor"));

        // Count total NGOs
        stats.put("totalNGOs", userRepository.countByType("ngo"));

        // Count total donations
        stats.put("totalDonations", donationRepository.count());

        // Count active (unclaimed) donations
        stats.put("activeDonations", donationRepository.countByClaimed(false));

        return stats;
    }
}