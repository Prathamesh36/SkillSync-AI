package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.JobApplicationResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.Application;
import com.codingshuttle.hackathon.skillsyncai.entity.Candidate;
import com.codingshuttle.hackathon.skillsyncai.entity.Job;
import com.codingshuttle.hackathon.skillsyncai.entity.Resume;
import com.codingshuttle.hackathon.skillsyncai.entity.User;
import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;
import com.codingshuttle.hackathon.skillsyncai.exception.BadRequestException;
import com.codingshuttle.hackathon.skillsyncai.exception.ResourceNotFoundException;
import com.codingshuttle.hackathon.skillsyncai.mapper.ApplicationMapper;
import com.codingshuttle.hackathon.skillsyncai.repository.ApplicationRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.CandidateRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.JobRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.MatchResultRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.ResumeRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing job applications.
 * Business logic is separated from controller layer.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final CandidateRepository candidateRepository;
    private final MatchResultRepository matchResultRepository;
    private final ApplicationMapper applicationMapper;

    /**
     * Apply for a job as a candidate.
     * - Validates job, candidate, and resume.
     * - Ensures resume belongs to the candidate.
     * - Prevents duplicate applications.
     * - Optionally fetches existing match score (NO AI call here).
     */
    @Transactional
    public JobApplicationResponseDTO applyForJob(String candidateEmail, Long jobId, Long resumeId) {
        log.info("Processing job application: candidateEmail={}, jobId={}, resumeId={}", candidateEmail, jobId,
                resumeId);

        // 1. Validate Job exists
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        // 2. Get User and Candidate
        User user = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + candidateEmail));

        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(
                        () -> new BadRequestException("Candidate profile not found. Please complete your profile."));

        // 3. Validate Resume exists and belongs to the candidate
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));

        if (!resume.getUser().getId().equals(user.getId())) {
            log.warn("Resume ownership validation failed: resumeId={} does not belong to userId={}", resumeId,
                    user.getId());
            throw new BadRequestException("The specified resume does not belong to you.");
        }

        // 4. Check for duplicate application
        if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidate.getId())) {
            log.warn("Duplicate application attempt: candidateId={}, jobId={}", candidate.getId(), jobId);
            throw new BadRequestException("You have already applied for this job.");
        }

        // 5. Fetch existing match score snapshot (if available from MatchResult)
        Double matchScoreSnapshot = matchResultRepository.findByJobIdAndCandidateId(jobId, candidate.getId())
                .map(result -> result.getMatchScore())
                .orElse(null);

        // 6. Create and save application
        Application application = new Application();
        application.setJob(job);
        application.setCandidate(candidate);
        application.setResume(resume);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setMatchScoreSnapshot(matchScoreSnapshot);

        Application saved = applicationRepository.save(application);
        log.info("Application created successfully: applicationId={}", saved.getId());

        return applicationMapper.toDTO(saved);
    }

    /**
     * Get all applications for the currently logged-in candidate.
     */
    public List<JobApplicationResponseDTO> getMyApplications(String candidateEmail) {
        log.debug("Fetching applications for candidate: {}", candidateEmail);

        User user = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + candidateEmail));

        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Candidate profile not found."));

        return applicationRepository.findByCandidateId(candidate.getId()).stream()
                .map(applicationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all applications for a job (Recruiter only).
     * Validates that the recruiter owns the job.
     */
    public List<JobApplicationResponseDTO> getApplicationsForJob(Long jobId, String recruiterEmail) {
        log.debug("Fetching applications for jobId={}, recruiterEmail={}", jobId, recruiterEmail);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        // Validate ownership
        if (!job.getPostedBy().getEmail().equals(recruiterEmail)) {
            log.warn("Unauthorized access attempt: recruiterEmail={} tried to access jobId={}", recruiterEmail, jobId);
            throw new AccessDeniedException("You are not authorized to view applications for this job.");
        }

        return applicationRepository.findByJobId(jobId).stream()
                .map(applicationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update application status (Recruiter only).
     * Validates that the recruiter owns the job.
     */
    @Transactional
    public JobApplicationResponseDTO updateApplicationStatus(Long applicationId, ApplicationStatus newStatus,
            String recruiterEmail) {
        log.info("Updating application status: applicationId={}, newStatus={}, recruiterEmail={}", applicationId,
                newStatus, recruiterEmail);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        // Validate ownership
        if (!application.getJob().getPostedBy().getEmail().equals(recruiterEmail)) {
            log.warn("Unauthorized status update attempt: recruiterEmail={} tried to update applicationId={}",
                    recruiterEmail, applicationId);
            throw new AccessDeniedException("You are not authorized to update this application.");
        }

        // Basic status transition validation
        validateStatusTransition(application.getStatus(), newStatus);

        application.setStatus(newStatus);
        Application updated = applicationRepository.save(application);
        log.info("Application status updated: applicationId={}, newStatus={}", applicationId, newStatus);

        return applicationMapper.toDTO(updated);
    }

    /**
     * Basic validation for status transitions.
     * Prevents illogical transitions like HIRED -> APPLIED.
     */
    private void validateStatusTransition(ApplicationStatus currentStatus, ApplicationStatus newStatus) {
        // Cannot transition from terminal states
        if (currentStatus == ApplicationStatus.HIRED || currentStatus == ApplicationStatus.WITHDRAWN) {
            throw new BadRequestException("Cannot change status from: " + currentStatus);
        }

        // Cannot go backwards (basic check)
        if (newStatus == ApplicationStatus.APPLIED && currentStatus != ApplicationStatus.APPLIED) {
            throw new BadRequestException("Cannot revert status to APPLIED.");
        }
    }
}
