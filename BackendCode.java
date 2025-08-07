/*
    This file contains the necessary Spring Boot code to handle the emergency report from the Android app.
    You can integrate this code into your existing Spring Boot application.
*/

// ============== EmergencyController.java ==============
// Make sure to place this in your `com.example.post.test.controller` package
// or update the package declaration to match your project structure.

package com.example.post.test.controller;

import com.example.post.test.DTOs.EmergencyDto;
import com.example.post.test.service.EmergencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emergencies")
public class EmergencyController {

    @Autowired
    private EmergencyService emergencyService;

    // ... (your existing controller methods)

    @PostMapping("/panic")
    public ResponseEntity<EmergencyDto> handlePanic(@RequestBody EmergencyDto dto) {
        // The logic for this is already in your provided EmergencyController.
        // This is just a placeholder to show where the endpoint should be.
        // You can adapt your existing `/panic` endpoint if needed.
        User reporter = userService.findByEmail(dto.getReporter().getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Emergency emergency = new Emergency();
        emergency.setReporter(reporter);
        emergency.setDescription(dto.getDescription());
        emergency.setLatitude(dto.getLatitude());
        emergency.setLongitude(dto.getLongitude());
        emergency.setLocationDescription(dto.getLocationDescription());
        emergency.setStatus(dto.getStatus());
        emergency.setReportedAt(dto.getReportedAt());

        emergencyService.saveEmergency(emergency);
        return ResponseEntity.ok().body(modelMapper.map(emergency, EmergencyDto.class));
    }
}


// ============== EmergencyDto.java ==============
// Make sure this DTO matches the structure expected by your application.
// Place this in your `com.example.post.test.DTOs` package.

package com.example.post.test.DTOs;

import lombok.Data;

@Data
public class EmergencyDto {
    private Long id;
    private String description;
    private String status;
    private Double latitude;
    private Double longitude;
    private String locationDescription;
    private String reportedAt;
    private UserDto reporter; // This should be a DTO, not the entity
}


// ============== UserDto.java ==============
// A simple User DTO. Place this in your `com.example.post.test.DTOs` package.

package com.example.post.test.DTOs;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String email;
    // Add other user fields as needed
}
