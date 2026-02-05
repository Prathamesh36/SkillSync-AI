package com.codingshuttle.hackathon.skillsyncai.controller;

import com.codingshuttle.hackathon.skillsyncai.dto.ApplicationResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;
import com.codingshuttle.hackathon.skillsyncai.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/apply")
    public ResponseEntity<ApplicationResponseDTO> applyForJob(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long jobId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.applyForJob(userId, jobId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(applicationService.getApplicationsByUser(userId));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationResponseDTO>> getApplicationsForJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(applicationService.getApplicationsForJob(jobId));
    }

    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationResponseDTO> updateStatus(
            @PathVariable Long applicationId,
            @RequestParam ApplicationStatus status) {
        return ResponseEntity.ok(applicationService.updateStatus(applicationId, status));
    }
}
