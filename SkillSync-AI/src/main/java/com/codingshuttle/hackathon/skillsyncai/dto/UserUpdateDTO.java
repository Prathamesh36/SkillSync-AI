package com.codingshuttle.hackathon.skillsyncai.dto;

import java.util.List;

public record UserUpdateDTO(
        String name,
        String bio,
        String linkedInUrl,
        String portfolioUrl,
        List<String> skills,
        Integer experienceYears) {
}
