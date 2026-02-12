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
                application.getCandidate().getUser().getEmail(),
                application.getResume().getId(),
                application.getStatus(),
                application.getAiAnalysis(),
                application.getMatchScoreSnapshot() != null
                        ? (int) Math.round(application.getMatchScoreSnapshot() * 100)
                        : 0,
                application.getAppliedAt(),
                null, // updatedAt not available in entity yet
                application.getJob().getPostedBy().getId());
    }
}
