package com.codingshuttle.hackathon.skillsyncai.controller;

import com.codingshuttle.hackathon.skillsyncai.dto.InterviewResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.JobApplicationRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.JobApplicationResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.RecruiterStatsDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.ScheduleInterviewRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.StatusUpdateRequestDTO;
import com.codingshuttle.hackathon.skillsyncai.service.ApplicationService;
import com.codingshuttle.hackathon.skillsyncai.service.InterviewScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Applications", description = "Job application lifecycle — apply, review, shortlist, and schedule interviews")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final InterviewScheduleService interviewScheduleService;

    @PostMapping("/api/jobs/{jobId}/apply")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Apply for a job", description = "Candidate submits an application with a previously uploaded resume")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Application submitted"),
            @ApiResponse(responseCode = "400", description = "Already applied or invalid resume"),
            @ApiResponse(responseCode = "404", description = "Job or resume not found")
    })
    public ResponseEntity<JobApplicationResponseDTO> applyForJob(
            Authentication authentication,
            @PathVariable Long jobId,
            @Valid @RequestBody JobApplicationRequestDTO request) {

        log.info("Apply request: jobId={}, resumeId={}", jobId, request.resumeId());
        String email = authentication.getName();
        JobApplicationResponseDTO response = applicationService.applyForJob(email, jobId, request.resumeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/candidates/me/applications")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get my applications", description = "Lists all job applications submitted by the current candidate")
    @ApiResponse(responseCode = "200", description = "List of applications")
    public ResponseEntity<List<JobApplicationResponseDTO>> getMyApplications(Authentication authentication) {
        String email = authentication.getName();
        log.debug("Fetching applications for candidate: {}", email);
        return ResponseEntity.ok(applicationService.getMyApplications(email));
    }

    @GetMapping("/api/candidates/me/interviews")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get my scheduled interviews", description = "Lists all scheduled interviews for the current candidate")
    @ApiResponse(responseCode = "200", description = "List of interviews")
    public ResponseEntity<List<InterviewResponseDTO>> getMyCandidateInterviews(Authentication authentication) {
        String email = authentication.getName();
        log.debug("Fetching interviews for candidate: {}", email);
        return ResponseEntity.ok(interviewScheduleService.getCandidateInterviews(email));
    }

    @GetMapping("/api/recruiter/jobs/{jobId}/applications")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Get applications for a job", description = "Recruiter views applications for a specific job, optionally filtered by status")
    @ApiResponse(responseCode = "200", description = "List of applications")
    public ResponseEntity<List<JobApplicationResponseDTO>> getJobApplications(
            Authentication authentication,
            @PathVariable Long jobId,
            @RequestParam(required = false) com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus status) {

        String email = authentication.getName();
        log.debug("Fetching applications for jobId={}, recruiter={}, status={}", jobId, email, status);
        return ResponseEntity.ok(applicationService.getApplicationsForJob(jobId, email, status));
    }

    @GetMapping("/api/recruiter/applications")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Get all recruiter applications", description = "Returns all applications across all jobs posted by the recruiter")
    @ApiResponse(responseCode = "200", description = "List of all applications")
    public ResponseEntity<List<JobApplicationResponseDTO>> getAllJobApplicationsForRecruiter(
            Authentication authentication) {
        String email = authentication.getName();
        log.debug("Fetching all applications for recruiter: {}", email);
        return ResponseEntity.ok(applicationService.getApplicationsForRecruiter(email));
    }

    @GetMapping("/api/recruiter/jobs/{jobId}/interviews")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Get interviews for a job", description = "Lists all scheduled interviews for a specific job")
    @ApiResponse(responseCode = "200", description = "List of interviews")
    public ResponseEntity<List<InterviewResponseDTO>> getJobInterviews(
            Authentication authentication,
            @PathVariable Long jobId) {

        String email = authentication.getName();
        log.debug("Fetching interviews for jobId={}, recruiter={}", jobId, email);
        return ResponseEntity.ok(interviewScheduleService.getRecruiterInterviewsForJob(jobId, email));
    }

    @GetMapping("/api/recruiter/interviews")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Get all recruiter interviews", description = "Lists all scheduled interviews across all recruiter's jobs")
    @ApiResponse(responseCode = "200", description = "List of interviews")
    public ResponseEntity<List<InterviewResponseDTO>> getAllRecruiterInterviews(Authentication authentication) {
        String email = authentication.getName();
        log.debug("Fetching all interviews for recruiter: {}", email);
        return ResponseEntity.ok(interviewScheduleService.getInterviewsForRecruiter(email));
    }

    @GetMapping("/api/recruiter/stats")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Get recruiter dashboard stats", description = "Returns aggregated statistics: active jobs, total applications, scheduled interviews")
    @ApiResponse(responseCode = "200", description = "Dashboard statistics")
    public ResponseEntity<RecruiterStatsDTO> getRecruiterStats(Authentication authentication) {
        String email = authentication.getName();
        log.debug("Fetching stats for recruiter: {}", email);
        return ResponseEntity.ok(applicationService.getRecruiterStats(email));
    }

    @PatchMapping("/api/applications/{applicationId}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Update application status", description = "Changes status (e.g., REVIEWED, REJECTED, OFFERED)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    public ResponseEntity<JobApplicationResponseDTO> updateApplicationStatus(
            Authentication authentication,
            @PathVariable Long applicationId,
            @Valid @RequestBody StatusUpdateRequestDTO request) {

        String email = authentication.getName();
        log.info("Status update: applicationId={}, newStatus={}, recruiter={}", applicationId, request.status(), email);
        return ResponseEntity.ok(applicationService.updateApplicationStatus(applicationId, request.status(), email));
    }

    @PatchMapping("/api/applications/{applicationId}/shortlist")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Shortlist a candidate", description = "Marks an application as SHORTLISTED for further review")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application shortlisted"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    public ResponseEntity<JobApplicationResponseDTO> shortlistCandidate(
            Authentication authentication,
            @PathVariable Long applicationId) {

        String email = authentication.getName();
        log.info("Shortlisting application: applicationId={}, recruiter={}", applicationId, email);
        return ResponseEntity.ok(applicationService.shortlistCandidate(applicationId, email));
    }

    @PostMapping("/api/applications/{applicationId}/schedule-interview")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Schedule an interview", description = "Schedules an interview for a shortlisted candidate. Sends email notification with .ics calendar invite.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Interview scheduled"),
            @ApiResponse(responseCode = "400", description = "Application not in SHORTLISTED status"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
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
