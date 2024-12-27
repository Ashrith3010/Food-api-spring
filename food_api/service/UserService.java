package com.food_api.food_api.service;

import com.food_api.food_api.dto.UserDTO;
import com.food_api.food_api.dto.credentials.LoginRequest;
import com.food_api.food_api.dto.credentials.LoginResponse;
import com.food_api.food_api.dto.RegisterRequest;
import com.food_api.food_api.entity.User;
import com.food_api.food_api.repository.UserRepository;
import com.food_api.food_api.service.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        try {
            // Find user by username, email, or phone
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseGet(() -> userRepository.findByEmail(request.getUsername())
                            .orElseGet(() -> userRepository.findByPhone(request.getUsername())
                                    .orElseThrow(() -> new UsernameNotFoundException("User not found"))));

            // Verify password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                LOGGER.warn("Invalid credentials for user: {}", request.getUsername());
                return LoginResponse.createErrorResponse("Invalid credentials");
            }

            // Generate JWT token
            String token = jwtService.generateToken(user);

            LOGGER.info("Login successful for user: {}", user.getUsername());

            // Create successful response
            return new LoginResponse(
                    true,
                    token,
                    "Login successful",
                    user.getType(),
                    user.getUsername()
            );
        } catch (Exception e) {
            LOGGER.error("Login error: {}", e.getMessage());
            return LoginResponse.createErrorResponse(e.getMessage());
        }
    }

    public void register(RegisterRequest request) {
        validateRegisterRequest(request);

        try {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setType(request.getUserType());
            user.setCreatedAt(LocalDateTime.now());

            if ("ngo".equalsIgnoreCase(request.getUserType())) {
                user.setOrganization(request.getOrganization());
                user.setArea(request.getArea());
            } else if ("donor".equalsIgnoreCase(request.getUserType())) {
                if (request.getOrganization() != null || request.getArea() != null) {
                    throw new IllegalArgumentException("Donor accounts cannot have organization or area fields.");
                }
            }

            userRepository.save(user);
            LOGGER.info("User registered successfully: {}", user.getUsername());
        } catch (Exception e) {
            LOGGER.error("Registration error: {}", e.getMessage());
            throw new RuntimeException("Registration failed. Please try again.");
        }
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request.getUsername() == null || request.getPassword() == null ||
                request.getEmail() == null || request.getPhone() == null) {
            throw new IllegalArgumentException("All fields (username, password, email, phone) are required.");
        }

        if (!request.getPhone().matches("\\d{10}")) {
            throw new IllegalArgumentException("Please enter a valid 10-digit phone number.");
        }

        if (!"ngo".equalsIgnoreCase(request.getUserType()) && !"donor".equalsIgnoreCase(request.getUserType())) {
            throw new IllegalArgumentException("Invalid account type. Only 'ngo' or 'donor' are allowed.");
        }

        if ("ngo".equalsIgnoreCase(request.getUserType())) {
            if (request.getOrganization() == null || request.getArea() == null) {
                throw new IllegalArgumentException("Organization and area are required for NGO accounts.");
            }
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists.");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists.");
        }
    }
    public List<UserDTO> getNGOs() {
        return userRepository.findByType("ngo")
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setType(user.getType());
        dto.setOrganization(user.getOrganization());
        dto.setArea(user.getArea());
        dto.setCreatedAt(user.getCreatedAt());
        // Note: password is intentionally not included in DTO
        return dto;
    }
};