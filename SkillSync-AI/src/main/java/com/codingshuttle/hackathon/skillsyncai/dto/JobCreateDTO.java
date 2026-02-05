package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.EmploymentType;
import com.codingshuttle.hackathon.skillsyncai.enums.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record JobCreateDTO(
                @NotBlank String title,

                @NotBlank String description,

                @NotBlank String companyName,

                @NotBlank String location,
                BigDecimal salaryMin,
                BigDecimal salaryMax,
                String currency,
                @NotNull JobType jobType,
                @NotNull EmploymentType employmentType,
                Integer requiredExperienceYears,
                List<String> skillsRequired) {
}
