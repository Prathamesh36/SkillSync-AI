package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponseDTO(
        Long id,
        String email,
        String name,
        UserRole role,
        String bio,
        String linkedInUrl,
        String portfolioUrl,
        List<String> skills,
        Integer experienceYears,
        LocalDateTime createdAt) {
}
