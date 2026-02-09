package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;

import java.math.BigDecimal;

public record RecommendedJobResponse(
        Long jobId,
        String jobTitle,
        String companyName,
        String location,
        Integer minExperience,
        Double matchScore,
        String shortAiExplanation,
        String jobType, // REMOTE, ONSITE, HYBRID
        String employmentType, // FULL_TIME, PART_TIME etc
        String applicationStatus, // "APPLY_NOW", "APPLIED", "INVITED"
        java.util.List<String> skills) {
}
