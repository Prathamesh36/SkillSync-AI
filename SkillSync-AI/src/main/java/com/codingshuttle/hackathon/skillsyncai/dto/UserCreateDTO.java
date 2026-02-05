package com.codingshuttle.hackathon.skillsyncai.dto;

import com.codingshuttle.hackathon.skillsyncai.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UserCreateDTO(
                @NotBlank @Email String email,
                @NotBlank String password,
                @NotBlank String name,
                @NotNull UserRole role,
                String bio,
                String linkedInUrl,
                String portfolioUrl,
                List<String> skills,
                Integer experienceYears,
                String headline,
                String location,
                String companyName,
                String designation,
                String companyWebsite) {
}
