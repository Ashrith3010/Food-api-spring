package com.food_api.food_api.controller;

import com.food_api.food_api.entity.User;
import com.food_api.food_api.repository.UserRepository;
import com.food_api.food_api.service.ApiResponse;
import com.food_api.food_api.service.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/account")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    // Get User Profile
    @GetMapping("/profile")
    public ApiResponse getUserProfile() {
        // Extracting the current user's information from the SecurityContext
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (!userOptional.isPresent()) {
            return new ApiResponse(false, "User not found");
        }

        User user = userOptional.get();

        // Remove sensitive information before sending the response
        user.setPassword(null); // Do not return password in the response

        return new ApiResponse(true, "User profile fetched successfully", user);
    }

    // Update User Profile
    @PutMapping("/profile")
    public ApiResponse updateUserProfile(@RequestBody User updatedUser) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> existingUserOptional = userRepository.findByUsername(username);

        if (!existingUserOptional.isPresent()) {
            return new ApiResponse(false, "User not found");
        }

        User existingUser = existingUserOptional.get();

        // Validate unique credentials (username, email, phone)
        // Username validation
        if (!existingUser.getUsername().equals(updatedUser.getUsername()) && userRepository.existsByUsername(updatedUser.getUsername())) {
            return new ApiResponse(false, "Username already in use");
        }

        // Email validation
        if (!existingUser.getEmail().equals(updatedUser.getEmail()) && userRepository.existsByEmail(updatedUser.getEmail())) {
            return new ApiResponse(false, "Email already in use");
        }

        // Phone number validation
        if (!existingUser.getPhone().equals(updatedUser.getPhone()) && userRepository.existsByPhone(updatedUser.getPhone())) {
            return new ApiResponse(false, "Phone number already in use");
        }

        // Update the user profile fields
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setOrganization(updatedUser.getOrganization());
        existingUser.setArea(updatedUser.getArea());

        // Save the updated user
        userRepository.save(existingUser);

        return new ApiResponse(true, "Profile updated successfully");
    }
}
