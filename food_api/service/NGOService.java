package com.food_api.food_api.service;
import com.food_api.food_api.dto.credentials.NGOResponse;
import com.food_api.food_api.entity.User;
import com.food_api.food_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NGOService {

    @Autowired
    private UserRepository userRepository;

    public List<NGOResponse> getAllNGOs() {
        return userRepository.findAll().stream()
                .filter(user -> "ngo".equalsIgnoreCase(user.getType()))
                .map(this::convertToNGOResponse)
                .collect(Collectors.toList());
    }

    private NGOResponse convertToNGOResponse(User user) {
        return new NGOResponse(
                user.getId(),
                user.getUsername(),
                user.getOrganization(),
                user.getArea(),
                user.getPhone(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}