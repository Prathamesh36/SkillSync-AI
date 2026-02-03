package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.EmploymentType;
import com.codingshuttle.hackathon.skillsyncai.enums.JobType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record JobResponseDTO(
        Long id,
        String title,
        String description,
        String companyName,
        String location,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        String currency,
        JobType jobType,
        EmploymentType employmentType,
        Integer requiredExperienceYears,
        List<String> skillsRequired,
        Long recruiterId,
        boolean active,
        LocalDateTime createdAt) {
}
