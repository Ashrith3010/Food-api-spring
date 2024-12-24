package com.food_api.food_api.service;

import com.food_api.food_api.dto.credentials.LoginRequest;
import com.food_api.food_api.dto.credentials.LoginResponse;
import com.food_api.food_api.dto.RegisterRequest;
import com.food_api.food_api.entity.User;
import com.food_api.food_api.repository.UserRepository;
import com.food_api.food_api.service.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
public class UserService {

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
                return LoginResponse.createErrorResponse("Invalid credentials");
            }

            // Generate JWT token
            String token = jwtService.generateToken(user);

            // Create successful response
            return new LoginResponse(
                    true,
                    token,
                    "Login successful",
                    user.getType(),
                    user.getUsername()
            );
        } catch (Exception e) {
            return LoginResponse.createErrorResponse(e.getMessage());
        }
    }

    public void register(RegisterRequest request) {
        // Validate required fields
        if (request.getUsername() == null || request.getPassword() == null ||
                request.getEmail() == null || request.getPhone() == null) {
            throw new IllegalArgumentException("All fields (username, password, email, phone) are required");
        }

        // Validate phone number format
        if (!request.getPhone().matches("\\d{10}")) {
            throw new IllegalArgumentException("Please enter a valid 10-digit phone number");
        }

        // Check for existing credentials
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setType(request.getUserType());
        user.setCreatedAt(LocalDateTime.now());

        // Set NGO specific fields if applicable
        if ("ngo".equals(request.getUserType())) {
            user.setOrganization(request.getOrganization());
            user.setArea(request.getArea());
        }

        // Save user
        userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }
}