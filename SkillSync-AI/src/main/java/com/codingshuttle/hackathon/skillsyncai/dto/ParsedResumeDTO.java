package com.codingshuttle.hackathon.skillsyncai.dto;

import java.util.List;

public record ParsedResumeDTO(
        String fullName,
        String email,
        List<String> skills,
        Integer experienceYears,
        String education,
        String summary) {
}
