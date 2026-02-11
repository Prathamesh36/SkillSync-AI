package com.codingshuttle.hackathon.skillsyncai.controller;

import com.codingshuttle.hackathon.skillsyncai.dto.MatchedCandidateDTO;
import com.codingshuttle.hackathon.skillsyncai.service.JobMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobMatchController {

    private final JobMatchingService jobMatchingService;

    @GetMapping("/{jobId}/matches")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<List<MatchedCandidateDTO>> getMatchedCandidates(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "10") int top,
            org.springframework.security.core.Authentication authentication) {

        List<MatchedCandidateDTO> matches = jobMatchingService.getMatchedCandidates(jobId, top,
                authentication.getName());
        return ResponseEntity.ok(matches);
    }
}
