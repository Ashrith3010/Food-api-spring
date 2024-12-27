package com.food_api.food_api.controller;


import com.food_api.food_api.dto.credentials.NGOResponse;
import com.food_api.food_api.service.NGOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class NGOController {

    @Autowired
    private NGOService ngoService;

    @GetMapping("/ngos")
    public ResponseEntity<?> getNGODirectory() {
        try {
            List<NGOResponse> ngos = ngoService.getAllNGOs();
            return ResponseEntity.ok(ngos);
        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Server error"
                    ));
        }
    }
}