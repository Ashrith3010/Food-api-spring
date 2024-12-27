package com.food_api.food_api.repository;

import com.food_api.food_api.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByDonorId(Long donorId);
    List<Donation> findByClaimedFalseAndExpiryTimeAfter(LocalDateTime now);
    List<Donation> findByClaimed(boolean claimed);
    List<Donation> findByLocationIgnoreCase(String location);
    long countByClaimed(boolean claimed);


}