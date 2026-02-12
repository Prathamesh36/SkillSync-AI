package com.codingshuttle.hackathon.skillsyncai.mapper;

import com.codingshuttle.hackathon.skillsyncai.dto.CandidateProfileDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.RecruiterProfileDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.UserCreateDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.UserResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.UserUpdateDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.Candidate;
import com.codingshuttle.hackathon.skillsyncai.entity.Recruiter;
import com.codingshuttle.hackathon.skillsyncai.entity.User;
import com.codingshuttle.hackathon.skillsyncai.enums.UserRole;
import com.codingshuttle.hackathon.skillsyncai.repository.CandidateRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.RecruiterRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final CandidateRepository candidateRepository;
    private final RecruiterRepository recruiterRepository;
    private final ResumeRepository resumeRepository;

    public User toEntity(UserCreateDTO dto) {
        User user = new User();
        user.setEmail(dto.email());
        user.setName(dto.name());
        user.setRole(dto.role());
        user.setBio(dto.bio());
        user.setLinkedInUrl(dto.linkedInUrl());
        user.setPortfolioUrl(dto.portfolioUrl());
        return user;
    }

    public void updateEntity(User user, UserUpdateDTO dto) {
        if (dto.name() != null)
            user.setName(dto.name());
        if (dto.bio() != null)
            user.setBio(dto.bio());
        if (dto.linkedInUrl() != null)
            user.setLinkedInUrl(dto.linkedInUrl());
        if (dto.portfolioUrl() != null)
            user.setPortfolioUrl(dto.portfolioUrl());
    }

    public void updateRecruiter(Recruiter recruiter, UserUpdateDTO dto) {
        if (dto.companyName() != null)
            recruiter.setCompanyName(dto.companyName());
        if (dto.designation() != null)
            recruiter.setDesignation(dto.designation());
        if (dto.companyWebsite() != null)
            recruiter.setCompanyWebsite(dto.companyWebsite());
    }

    public UserResponseDTO toDTO(User user) {
        CandidateProfileDTO candidateProfile = null;
        RecruiterProfileDTO recruiterProfile = null;

        if (user.getRole() == UserRole.CANDIDATE) {
            Candidate candidate = candidateRepository.findByUserId(user.getId()).orElse(null);
            if (candidate != null) {
                Long resumeId = resumeRepository.findByUserId(user.getId())
                        .map(com.codingshuttle.hackathon.skillsyncai.entity.Resume::getId)
                        .orElse(null);

                candidateProfile = new CandidateProfileDTO(
                        candidate.getSkills(),
                        candidate.getExperienceYears(),
                        candidate.getHeadline(),
                        candidate.getLocation(),
                        resumeId);
            }
        } else if (user.getRole() == UserRole.RECRUITER) {
            Recruiter recruiter = recruiterRepository.findByUserId(user.getId()).orElse(null);
            if (recruiter != null) {
                recruiterProfile = new RecruiterProfileDTO(
                        recruiter.getCompanyName(),
                        recruiter.getDesignation(),
                        recruiter.getCompanyWebsite());
            }
        }

        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                user.getBio(),
                user.getLinkedInUrl(),
                user.getPortfolioUrl(),
                candidateProfile,
                recruiterProfile,
                user.getCreatedAt());
    }
}
