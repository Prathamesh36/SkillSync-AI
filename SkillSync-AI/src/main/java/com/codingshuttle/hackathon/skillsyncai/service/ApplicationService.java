package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.JobApplicationResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.RecruiterStatsDTO;
import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;

import java.util.List;

public interface ApplicationService {
        JobApplicationResponseDTO applyForJob(String candidateEmail, Long jobId, Long resumeId);

        List<JobApplicationResponseDTO> getMyApplications(String candidateEmail);

        List<JobApplicationResponseDTO> getApplicationsForRecruiter(String recruiterEmail);

        List<JobApplicationResponseDTO> getApplicationsForJob(Long jobId, String recruiterEmail,
                        ApplicationStatus status);

        JobApplicationResponseDTO updateApplicationStatus(Long applicationId, ApplicationStatus newStatus,
                        String recruiterEmail);

        JobApplicationResponseDTO shortlistCandidate(Long applicationId, String recruiterEmail);

        RecruiterStatsDTO getRecruiterStats(String recruiterEmail);
}
