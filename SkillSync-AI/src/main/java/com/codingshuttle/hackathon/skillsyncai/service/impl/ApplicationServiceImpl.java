package com.codingshuttle.hackathon.skillsyncai.service.impl;

import com.codingshuttle.hackathon.skillsyncai.dto.JobApplicationResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.RecruiterStatsDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.*;
import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;
import com.codingshuttle.hackathon.skillsyncai.exception.BadRequestException;
import com.codingshuttle.hackathon.skillsyncai.exception.ResourceNotFoundException;
import com.codingshuttle.hackathon.skillsyncai.mapper.ApplicationMapper;
import com.codingshuttle.hackathon.skillsyncai.repository.*;
import com.codingshuttle.hackathon.skillsyncai.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final InterviewScheduleRepository interviewScheduleRepository;
    private final RecruiterRepository recruiterRepository;
    private final ResumeRepository resumeRepository;
    private final CandidateRepository candidateRepository;
    private final MatchResultRepository matchResultRepository;
    private final ApplicationMapper applicationMapper;

    @Override
    @Transactional
    public JobApplicationResponseDTO applyForJob(String candidateEmail, Long jobId, Long resumeId) {
        log.info("Processing job application: candidateEmail={}, jobId={}, resumeId={}", candidateEmail, jobId,
                resumeId);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        User user = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + candidateEmail));

        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(
                        () -> new BadRequestException("Candidate profile not found. Please complete your profile."));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("The specified resume does not belong to you.");
        }

        if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidate.getId())) {
            throw new BadRequestException("You have already applied for this job.");
        }

        Double matchScoreSnapshot = matchResultRepository.findByJobIdAndCandidateId(jobId, candidate.getId())
                .map(MatchResult::getMatchScore)
                .orElseGet(() -> calculateMatchScore(job, candidate));

        Application application = new Application();
        application.setJob(job);
        application.setCandidate(candidate);
        application.setResume(resume);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setMatchScoreSnapshot(matchScoreSnapshot);

        Application saved = applicationRepository.save(application);
        return applicationMapper.toDTO(saved);
    }

    private Double calculateMatchScore(Job job, Candidate candidate) {
        double skillScore = calculateSkillScore(job.getSkillsRequired(), candidate.getSkills());
        double expScore = calculateExperienceScore(job.getMinExperienceYears(), job.getMaxExperienceYears(),
                candidate.getExperienceYears());

        double finalScore = (0.7 * skillScore) + (0.3 * expScore);
        return Math.round(finalScore * 100.0 * 10.0) / 10.0;
    }

    private double calculateSkillScore(List<String> jobSkills, List<String> candidateSkills) {
        if (jobSkills == null || jobSkills.isEmpty())
            return 1.0;
        if (candidateSkills == null || candidateSkills.isEmpty())
            return 0.0;

        java.util.Set<String> jobSkillSet = jobSkills.stream().map(String::toLowerCase).collect(Collectors.toSet());
        java.util.Set<String> candidateSkillSet = candidateSkills.stream().map(String::toLowerCase)
                .collect(Collectors.toSet());

        long matchCount = jobSkillSet.stream().filter(candidateSkillSet::contains).count();
        return (double) matchCount / jobSkillSet.size();
    }

    private double calculateExperienceScore(Integer minExp, Integer maxExp, Integer candidateExp) {
        if ((minExp == null || minExp == 0) && (maxExp == null || maxExp == 0))
            return 1.0;
        if (candidateExp == null)
            return 0.0;

        int effectiveMin = (minExp != null) ? minExp : 0;
        int effectiveMax = (maxExp != null) ? maxExp : Integer.MAX_VALUE;

        if (candidateExp >= effectiveMin && candidateExp <= effectiveMax)
            return 1.0;
        if (candidateExp > effectiveMax)
            return 0.85;
        return Math.max(0.3, (double) candidateExp / effectiveMin);
    }

    @Override
    public List<JobApplicationResponseDTO> getMyApplications(String candidateEmail) {
        User user = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + candidateEmail));

        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Candidate profile not found."));

        return applicationRepository.findByCandidateId(candidate.getId()).stream()
                .map(applicationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobApplicationResponseDTO> getApplicationsForRecruiter(String recruiterEmail) {
        User user = userRepository.findByEmail(recruiterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.getRole().name().equals("RECRUITER")) {
            throw new AccessDeniedException("User is not a recruiter");
        }

        List<Application> applications = applicationRepository.findByJobPostedByIdOrderByAppliedAtDesc(user.getId());
        return applications.stream().map(applicationMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<JobApplicationResponseDTO> getApplicationsForJob(Long jobId, String recruiterEmail,
            ApplicationStatus status) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (!job.getPostedBy().getEmail().equals(recruiterEmail)) {
            throw new AccessDeniedException("You are not authorized to view applications for this job.");
        }

        List<Application> applications = applicationRepository.findByJobId(jobId);

        for (Application app : applications) {
            if (app.getMatchScoreSnapshot() == null) {
                Candidate candidate = candidateRepository.findByIdWithSkills(app.getCandidate().getId())
                        .orElse(app.getCandidate());
                Double score = calculateMatchScore(job, candidate);
                app.setMatchScoreSnapshot(score);
                applicationRepository.save(app);
            }
        }

        if (status != null) {
            applications = applications.stream().filter(app -> app.getStatus() == status).collect(Collectors.toList());
        }

        return applications.stream().map(applicationMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobApplicationResponseDTO updateApplicationStatus(Long applicationId, ApplicationStatus newStatus,
            String recruiterEmail) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        if (!application.getJob().getPostedBy().getEmail().equals(recruiterEmail)) {
            throw new AccessDeniedException("You are not authorized to update this application.");
        }

        validateStatusTransition(application.getStatus(), newStatus);
        application.setStatus(newStatus);
        Application updated = applicationRepository.save(application);
        log.info("Application status updated: applicationId={}, newStatus={}", applicationId, newStatus);

        return applicationMapper.toDTO(updated);
    }

    private void validateStatusTransition(ApplicationStatus currentStatus, ApplicationStatus newStatus) {
        if (currentStatus == ApplicationStatus.HIRED || currentStatus == ApplicationStatus.WITHDRAWN) {
            throw new BadRequestException("Cannot change status from: " + currentStatus);
        }
        if (newStatus == ApplicationStatus.APPLIED && currentStatus != ApplicationStatus.APPLIED) {
            throw new BadRequestException("Cannot revert status to APPLIED.");
        }
    }

    @Override
    @Transactional
    public JobApplicationResponseDTO shortlistCandidate(Long applicationId, String recruiterEmail) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        if (!application.getJob().getPostedBy().getEmail().equals(recruiterEmail)) {
            throw new AccessDeniedException("You are not authorized to shortlist this application.");
        }

        if (application.getStatus() != ApplicationStatus.APPLIED) {
            throw new BadRequestException("Can only shortlist applications with status APPLIED.");
        }

        application.setStatus(ApplicationStatus.SHORTLISTED);
        return applicationMapper.toDTO(applicationRepository.save(application));
    }

    @Override
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
