package com.codingshuttle.hackathon.skillsyncai.mapper;

import com.codingshuttle.hackathon.skillsyncai.dto.JobApplicationResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.Application;
import org.springframework.stereotype.Component;

/**
 * Mapper for Application entity to JobApplicationResponseDTO.
 */
@Component
public class ApplicationMapper {

    public JobApplicationResponseDTO toDTO(Application application) {
        return new JobApplicationResponseDTO(
                application.getId(),
                application.getJob().getId(),
                application.getJob().getTitle(),
                application.getJob().getCompanyName(),
                application.getCandidate().getId(),
                application.getCandidate().getUser().getName(),
                application.getResume().getId(),
                application.getStatus(),
                application.getMatchScoreSnapshot(),
                application.getAppliedAt(),
                application.getCandidate().getUser().getEmail(),
                application.getCandidate().getHeadline(),
                application.getCandidate().getSkills(),
                application.getCandidate().getLocation());
    }
}
