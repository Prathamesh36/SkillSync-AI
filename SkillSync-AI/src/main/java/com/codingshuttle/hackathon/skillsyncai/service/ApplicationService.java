package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.JobApplicationResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.RecruiterStatsDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.Application;
import com.codingshuttle.hackathon.skillsyncai.entity.Candidate;
import com.codingshuttle.hackathon.skillsyncai.entity.Job;
import com.codingshuttle.hackathon.skillsyncai.entity.Recruiter;
import com.codingshuttle.hackathon.skillsyncai.entity.Resume;
import com.codingshuttle.hackathon.skillsyncai.entity.User;
import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;
import com.codingshuttle.hackathon.skillsyncai.exception.BadRequestException;
import com.codingshuttle.hackathon.skillsyncai.exception.ResourceNotFoundException;
import com.codingshuttle.hackathon.skillsyncai.mapper.ApplicationMapper;
import com.codingshuttle.hackathon.skillsyncai.repository.ApplicationRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.CandidateRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.InterviewScheduleRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.JobRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.MatchResultRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.RecruiterRepository;
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
        private final InterviewScheduleRepository interviewScheduleRepository;
        private final RecruiterRepository recruiterRepository;
        private final ResumeRepository resumeRepository;
        private final CandidateRepository candidateRepository;
        private final MatchResultRepository matchResultRepository;
        private final ApplicationMapper applicationMapper;

        /**
         * Apply for a job as a candidate.
         * - Validates job, candidate, and resume.
         * - Ensures resume belongs to the candidate.
         * - Prevents duplicate applications.
         * - Calculates match score based on skills and experience if not already
         * present.
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
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found with email: " + candidateEmail));

                Candidate candidate = candidateRepository.findByUserId(user.getId())
                                .orElseThrow(
                                                () -> new BadRequestException(
                                                                "Candidate profile not found. Please complete your profile."));

                // 3. Validate Resume exists and belongs to the candidate
                Resume resume = resumeRepository.findById(resumeId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Resume not found with id: " + resumeId));

                if (!resume.getUser().getId().equals(user.getId())) {
                        log.warn("Resume ownership validation failed: resumeId={} does not belong to userId={}",
                                        resumeId,
                                        user.getId());
                        throw new BadRequestException("The specified resume does not belong to you.");
                }

                // 4. Check for duplicate application
                if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidate.getId())) {
                        log.warn("Duplicate application attempt: candidateId={}, jobId={}", candidate.getId(), jobId);
                        throw new BadRequestException("You have already applied for this job.");
                }

                // 5. Calculate or fetch match score
                Double matchScoreSnapshot = matchResultRepository.findByJobIdAndCandidateId(jobId, candidate.getId())
                                .map(result -> result.getMatchScore())
                                .orElseGet(() -> calculateMatchScore(job, candidate));

                // 6. Create and save application
                Application application = new Application();
                application.setJob(job);
                application.setCandidate(candidate);
                application.setResume(resume);
                application.setStatus(ApplicationStatus.APPLIED);
                application.setMatchScoreSnapshot(matchScoreSnapshot);

                Application saved = applicationRepository.save(application);
                log.info("Application created successfully: applicationId={}, matchScore={}", saved.getId(),
                                matchScoreSnapshot);

                return applicationMapper.toDTO(saved);
        }

        /**
         * Calculate match score based on skills overlap and experience.
         * Returns a score between 0.0 and 100.0
         */
        private Double calculateMatchScore(Job job, Candidate candidate) {
                double skillScore = calculateSkillScore(job.getSkillsRequired(), candidate.getSkills());
                double expScore = calculateExperienceScore(job.getRequiredExperienceYears(),
                                candidate.getExperienceYears());

                // Weighted combination: 70% skills, 30% experience
                double finalScore = (0.7 * skillScore) + (0.3 * expScore);

                // Convert to percentage (0-100)
                return Math.round(finalScore * 100.0 * 10.0) / 10.0;
        }

        private double calculateSkillScore(java.util.List<String> jobSkills, java.util.List<String> candidateSkills) {
                if (jobSkills == null || jobSkills.isEmpty())
                        return 1.0;
                if (candidateSkills == null || candidateSkills.isEmpty())
                        return 0.0;

                java.util.Set<String> jobSkillSet = jobSkills.stream()
                                .map(String::toLowerCase)
                                .collect(java.util.stream.Collectors.toSet());
                java.util.Set<String> candidateSkillSet = candidateSkills.stream()
                                .map(String::toLowerCase)
                                .collect(java.util.stream.Collectors.toSet());

                long matchCount = jobSkillSet.stream().filter(candidateSkillSet::contains).count();
                return (double) matchCount / jobSkillSet.size();
        }

        private double calculateExperienceScore(Integer jobExp, Integer candidateExp) {
                if (jobExp == null || jobExp == 0)
                        return 1.0;
                if (candidateExp == null)
                        return 0.0;

                if (candidateExp >= jobExp)
                        return 1.0;
                return (double) candidateExp / jobExp;
        }

        /**
         * Get all applications for the currently logged-in candidate.
         */
        public List<JobApplicationResponseDTO> getMyApplications(String candidateEmail) {
                log.debug("Fetching applications for candidate: {}", candidateEmail);

                User user = userRepository.findByEmail(candidateEmail)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found with email: " + candidateEmail));

                Candidate candidate = candidateRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new BadRequestException("Candidate profile not found."));

                return applicationRepository.findByCandidateId(candidate.getId()).stream()
                                .map(applicationMapper::toDTO)
                                .collect(Collectors.toList());
        }

        /**
         * Get all applications for all jobs posted by the recruiter.
         */
        @Transactional(readOnly = true)
        public List<JobApplicationResponseDTO> getApplicationsForRecruiter(String recruiterEmail) {
                log.debug("Fetching all applications for recruiter: {}", recruiterEmail);

                User user = userRepository.findByEmail(recruiterEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                // We need Recruiter profile simply to validate they are a recruiter,
                // but the job is linked via User ID (postedBy)
                if (!user.getRole().name().equals("RECRUITER")) {
                        throw new AccessDeniedException("User is not a recruiter");
                }

                List<Application> applications = applicationRepository
                                .findByJobPostedByIdOrderByAppliedAtDesc(user.getId());

                // Calculate missing match scores (optional, but good for consistency)
                // For now, simple mapping
                return applications.stream()
                                .map(applicationMapper::toDTO)
                                .collect(Collectors.toList());
        }

        /**
         * Get all applications for a job (Recruiter only).
         * Validates that the recruiter owns the job.
         * Optionally filters by status.
         * Calculates missing match scores for existing applications.
         */
        @Transactional
        public List<JobApplicationResponseDTO> getApplicationsForJob(Long jobId, String recruiterEmail,
                        ApplicationStatus status) {
                log.info("Fetching applications for jobId={}, recruiterEmail={}, status={}", jobId, recruiterEmail,
                                status);

                Job job = jobRepository.findById(jobId)
                                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

                // Validate ownership
                String jobPosterEmail = job.getPostedBy().getEmail();
                log.info("Job found: {}, posted by: {}", job.getTitle(), jobPosterEmail);

                if (!jobPosterEmail.equals(recruiterEmail)) {
                        log.warn("Unauthorized access attempt: recruiterEmail={} tried to access jobId={} (posted by {})",
                                        recruiterEmail, jobId, jobPosterEmail);
                        throw new AccessDeniedException("You are not authorized to view applications for this job.");
                }

                List<Application> applications = applicationRepository.findByJobId(jobId);
                log.info("Found {} applications for jobId={}", applications.size(), jobId);

                // Calculate and update missing match scores
                for (Application app : applications) {
                        if (app.getMatchScoreSnapshot() == null) {
                                // Fetch candidate with skills eagerly loaded
                                Candidate candidate = candidateRepository.findByIdWithSkills(app.getCandidate().getId())
                                                .orElse(app.getCandidate());
                                log.info("Calculating score for candidateId={}, skills={}, jobSkills={}",
                                                candidate.getId(), candidate.getSkills(), job.getSkillsRequired());
                                Double score = calculateMatchScore(job, candidate);
                                app.setMatchScoreSnapshot(score);
                                applicationRepository.save(app);
                                log.info("Calculated missing match score for applicationId={}: {}", app.getId(), score);
                        }
                }

                // Filter by status if provided
                if (status != null) {
                        applications = applications.stream()
                                        .filter(app -> app.getStatus() == status)
                                        .collect(Collectors.toList());
                        log.info("After filtering by status {}, {} applications remain", status, applications.size());
                }

                return applications.stream()
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
                log.info("Updating application status: applicationId={}, newStatus={}, recruiterEmail={}",
                                applicationId,
                                newStatus, recruiterEmail);

                Application application = applicationRepository.findById(applicationId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Application not found with id: " + applicationId));

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

        /**
         * Shortlist a candidate for a job.
         * Changes status from APPLIED to SHORTLISTED.
         * 
         * @param applicationId  the application to shortlist
         * @param recruiterEmail email of the recruiter (for ownership validation)
         * @return updated application details
         */
        @Transactional
        public JobApplicationResponseDTO shortlistCandidate(Long applicationId, String recruiterEmail) {
                log.info("Shortlisting application: applicationId={}, recruiterEmail={}", applicationId,
                                recruiterEmail);

                Application application = applicationRepository.findById(applicationId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Application not found with id: " + applicationId));

                // Validate recruiter owns the job
                if (!application.getJob().getPostedBy().getEmail().equals(recruiterEmail)) {
                        log.warn("Unauthorized shortlist attempt: recruiterEmail={} tried to shortlist applicationId={}",
                                        recruiterEmail, applicationId);
                        throw new AccessDeniedException("You are not authorized to shortlist this application.");
                }

                // Validate current status is APPLIED
                if (application.getStatus() != ApplicationStatus.APPLIED) {
                        throw new BadRequestException(
                                        "Can only shortlist applications with status APPLIED. Current status: "
                                                        + application.getStatus());
                }

                application.setStatus(ApplicationStatus.SHORTLISTED);
                Application updated = applicationRepository.save(application);
                log.info("Application shortlisted: applicationId={}", applicationId);

                return applicationMapper.toDTO(updated);
        }

        /**
         * Get dashboard stats for recruiter.
         */
        @Transactional(readOnly = true)
        public RecruiterStatsDTO getRecruiterStats(String recruiterEmail) {
                User user = userRepository.findByEmail(recruiterEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Recruiter recruiter = recruiterRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));

                long activeJobs = jobRepository.countByPostedByAndActiveTrue(user);
                long totalApplications = applicationRepository.countByJobPostedById(user.getId());
                long scheduledInterviews = interviewScheduleRepository.countByRecruiterIdAndStatus(
                                recruiter.getId(),
                                com.codingshuttle.hackathon.skillsyncai.enums.InterviewScheduleStatus.SCHEDULED);

                return new RecruiterStatsDTO(activeJobs, totalApplications, scheduledInterviews);
        }
}
