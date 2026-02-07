package com.codingshuttle.hackathon.skillsyncai.controller;

import com.codingshuttle.hackathon.skillsyncai.dto.InterviewResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.JobApplicationRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.JobApplicationResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.ScheduleInterviewRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.StatusUpdateRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.service.ApplicationService;
import com.codingshuttle.hackathon.skillsyncai.service.InterviewScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for job application operations.
 * Implements role-based access control using method security.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ApplicationController {

    private final ApplicationService applicationService;
    private final InterviewScheduleService interviewScheduleService;

    /**
     * Candidate applies for a job.
     * POST /jobs/{jobId}/apply
     */
    @PostMapping("/api/jobs/{jobId}/apply")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<JobApplicationResponseDTO> applyForJob(
            Authentication authentication,
            @PathVariable Long jobId,
            @Valid @RequestBody JobApplicationRequestDTO request) {

        log.info("Apply request: jobId={}, resumeId={}", jobId, request.resumeId());
        String email = authentication.getName();
        JobApplicationResponseDTO response = applicationService.applyForJob(email, jobId, request.resumeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Candidate views their own applications.
     * GET /candidates/me/applications
     */
    @GetMapping("/api/candidates/me/applications")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<JobApplicationResponseDTO>> getMyApplications(Authentication authentication) {
        String email = authentication.getName();
        log.debug("Fetching applications for candidate: {}", email);
        return ResponseEntity.ok(applicationService.getMyApplications(email));
    }

    /**
     * Candidate views their scheduled interviews.
     * GET /candidates/me/interviews
     */
    @GetMapping("/api/candidates/me/interviews")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<InterviewResponseDTO>> getMyCandidateInterviews(Authentication authentication) {
        String email = authentication.getName();
        log.debug("Fetching interviews for candidate: {}", email);
        return ResponseEntity.ok(interviewScheduleService.getCandidateInterviews(email));
    }

    /**
     * Recruiter views applications for a specific job.
     * GET /recruiter/jobs/{jobId}/applications
     */
    @GetMapping("/api/recruiter/jobs/{jobId}/applications")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<List<JobApplicationResponseDTO>> getJobApplications(
            Authentication authentication,
            @PathVariable Long jobId,
            @RequestParam(required = false) com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus status) {

        String email = authentication.getName();
        log.debug("Fetching applications for jobId={}, recruiter={}, status={}", jobId, email, status);
        return ResponseEntity.ok(applicationService.getApplicationsForJob(jobId, email, status));
    }

    /**
     * Recruiter views scheduled interviews for a job.
     * GET /recruiter/jobs/{jobId}/interviews
     */
    @GetMapping("/api/recruiter/jobs/{jobId}/interviews")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<List<InterviewResponseDTO>> getJobInterviews(
            Authentication authentication,
            @PathVariable Long jobId) {

        String email = authentication.getName();
        log.debug("Fetching interviews for jobId={}, recruiter={}", jobId, email);
        return ResponseEntity.ok(interviewScheduleService.getRecruiterInterviewsForJob(jobId, email));
    }

    /**
     * Recruiter updates application status.
     * PATCH /applications/{applicationId}/status
     */
    @PatchMapping("/api/applications/{applicationId}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobApplicationResponseDTO> updateApplicationStatus(
            Authentication authentication,
            @PathVariable Long applicationId,
            @Valid @RequestBody StatusUpdateRequestDTO request) {

        String email = authentication.getName();
        log.info("Status update: applicationId={}, newStatus={}, recruiter={}", applicationId, request.status(), email);
        return ResponseEntity.ok(applicationService.updateApplicationStatus(applicationId, request.status(), email));
    }

    /**
     * Recruiter shortlists a candidate.
     * PATCH /applications/{applicationId}/shortlist
     */
    @PatchMapping("/api/applications/{applicationId}/shortlist")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobApplicationResponseDTO> shortlistCandidate(
            Authentication authentication,
            @PathVariable Long applicationId) {

        String email = authentication.getName();
        log.info("Shortlisting application: applicationId={}, recruiter={}", applicationId, email);
        return ResponseEntity.ok(applicationService.shortlistCandidate(applicationId, email));
    }

    /**
     * Recruiter schedules an interview for a shortlisted candidate.
     * POST /applications/{applicationId}/schedule-interview
     */
    @PostMapping("/api/applications/{applicationId}/schedule-interview")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<InterviewResponseDTO> scheduleInterview(
            Authentication authentication,
            @PathVariable Long applicationId,
            @Valid @RequestBody ScheduleInterviewRequestDTO request) {

        String email = authentication.getName();
        log.info("Scheduling interview: applicationId={}, recruiter={}, dateTime={}",
                applicationId, email, request.interviewDateTime());
        InterviewResponseDTO response = interviewScheduleService.scheduleInterview(applicationId, request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
