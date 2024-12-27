package com.food_api.food_api.service.jwt;

import com.food_api.food_api.entity.User;
import com.food_api.food_api.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        // If there's no authorization header or it's not a Bearer token, continue the request
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract the JWT token from the header
            String jwt = authHeader.substring(7);

            // Validate the token
            if (jwtService.validateToken(jwt)) {
                Claims claims = jwtService.extractClaims(jwt);
                String username = claims.get("username", String.class);

                // If username is present and authentication is not already set
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    User user = userRepository.findByUsername(username)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    // Set user details from JWT claims and the database
                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        user.setId(Long.valueOf(jwtService.getUserIdFromToken(jwt)));
                        user.setEmail(jwtService.getEmailFromToken(jwt));
                        user.setPhone(jwtService.getPhoneNumberFromToken(jwt));

                        // Create authentication token
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                user.getAuthorities() // Make sure User implements UserDetails or has appropriate authorities
                        );

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("JWT Authentication error: ", e);
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}
