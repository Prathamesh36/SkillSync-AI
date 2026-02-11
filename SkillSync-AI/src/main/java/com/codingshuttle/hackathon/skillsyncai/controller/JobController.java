package com.codingshuttle.hackathon.skillsyncai.controller;

import com.codingshuttle.hackathon.skillsyncai.dto.JobCreateDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.JobResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.Job;
import com.codingshuttle.hackathon.skillsyncai.entity.User;
import com.codingshuttle.hackathon.skillsyncai.exception.ResourceNotFoundException;
import com.codingshuttle.hackathon.skillsyncai.mapper.JobMapper;
import com.codingshuttle.hackathon.skillsyncai.enums.EmploymentType;
import com.codingshuttle.hackathon.skillsyncai.enums.JobType;
import com.codingshuttle.hackathon.skillsyncai.repository.UserRepository;
import com.codingshuttle.hackathon.skillsyncai.service.JobService;
import com.codingshuttle.hackathon.skillsyncai.scheduler.JobScheduler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobService jobService;
    private final UserRepository userRepository;
    private final com.codingshuttle.hackathon.skillsyncai.repository.RecruiterRepository recruiterRepository;
    private final JobMapper jobMapper;
    private final JobScheduler jobScheduler;

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobResponseDTO> createJob(
            Authentication authentication,
            @Valid @RequestBody JobCreateDTO dto) {

        String email = authentication.getName();
        User recruiterUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found with email: " + email));

        com.codingshuttle.hackathon.skillsyncai.entity.Recruiter recruiterProfile = recruiterRepository
                .findByUserId(recruiterUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));

        List<String> missingFields = new java.util.ArrayList<>();
        if (recruiterUser.getName() == null || recruiterUser.getName().trim().isEmpty())
            missingFields.add("Name");
        if (recruiterUser.getBio() == null || recruiterUser.getBio().trim().isEmpty())
            missingFields.add("Bio");
        if (recruiterUser.getLinkedInUrl() == null || recruiterUser.getLinkedInUrl().trim().isEmpty())
            missingFields.add("LinkedIn URL");

        if (recruiterProfile.getCompanyName() == null || recruiterProfile.getCompanyName().trim().isEmpty())
            missingFields.add("Company Name");
        if (recruiterProfile.getDesignation() == null || recruiterProfile.getDesignation().trim().isEmpty())
            missingFields.add("Designation");
        if (recruiterProfile.getCompanyWebsite() == null || recruiterProfile.getCompanyWebsite().trim().isEmpty())
            missingFields.add("Company Website");

        if (!missingFields.isEmpty()) {
            throw new com.codingshuttle.hackathon.skillsyncai.exception.BadRequestException(
                    "Profile must be 100% complete to post a job. Missing: " + String.join(", ", missingFields));
        }

        Job job = jobMapper.toEntity(dto);
        job.setPostedBy(recruiterUser);
        job.setCompanyName(recruiterProfile.getCompanyName());

        Job created = jobService.createJob(job);
        return ResponseEntity.status(HttpStatus.CREATED).body(jobMapper.toDTO(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponseDTO> getJob(@PathVariable Long id) {
        Job job = jobService.getJob(id);
        return ResponseEntity.ok(jobMapper.toDTO(job));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<List<JobResponseDTO>> getMyJobs(Authentication authentication) {
        String email = authentication.getName();
        User recruiter = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));
        List<Job> jobs = jobService.getJobsByRecruiter(recruiter);
        return ResponseEntity.ok(jobs.stream().map(jobMapper::toDTO).collect(Collectors.toList()));
    }

    @GetMapping
    public ResponseEntity<List<JobResponseDTO>> getAllJobs() {
        List<Job> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(jobs.stream().map(jobMapper::toDTO).collect(Collectors.toList()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobResponseDTO> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobCreateDTO dto,
            Authentication authentication) {

        Job existingJob = jobService.getJob(id);

        if (!existingJob.getPostedBy().getEmail().equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        jobMapper.updateEntityFromDTO(dto, existingJob);

        Job updated = jobService.updateJob(id, existingJob);
        return ResponseEntity.ok(jobMapper.toDTO(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id, Authentication authentication) {
        Job existingJob = jobService.getJob(id);

        if (!existingJob.getPostedBy().getEmail().equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<JobResponseDTO>> searchJobs(@RequestParam String query) {
        List<Job> jobs = jobService.searchJobs(query);
        return ResponseEntity.ok(jobs.stream().map(jobMapper::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<JobResponseDTO>> filterJobs(
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) EmploymentType employmentType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary,
            @RequestParam(required = false) String skill) {

        List<Job> jobs = jobService.filterJobs(jobType, employmentType, location, minSalary, maxSalary, skill);
        return ResponseEntity.ok(jobs.stream().map(jobMapper::toDTO).collect(Collectors.toList()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobResponseDTO> toggleJobStatus(
            @PathVariable Long id,
            @RequestParam boolean active,
            Authentication authentication) {

        Job job = jobService.getJob(id);

        // Ownership check
        if (!job.getPostedBy().getEmail().equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        job.setActive(active);
        Job updated = jobService.updateJob(id, job);
        return ResponseEntity.ok(jobMapper.toDTO(updated));
    }

    @PostMapping("/scheduler/trigger-expiry")
    public ResponseEntity<String> triggerJobExpiry() {
        try {
            System.err.println("JobController: Triggering expiry...");
            jobScheduler.closeExpiredJobs();
            System.err.println("JobController: Expiry triggered successfully.");
            return ResponseEntity.ok("Success");
        } catch (Throwable e) {
            System.err.println("JobController: Error triggering expiry: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
