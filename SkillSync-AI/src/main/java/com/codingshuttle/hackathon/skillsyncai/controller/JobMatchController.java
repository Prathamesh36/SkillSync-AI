package com.codingshuttle.hackathon.skillsyncai.controller;

import com.codingshuttle.hackathon.skillsyncai.dto.MatchedCandidateDTO;
import com.codingshuttle.hackathon.skillsyncai.service.JobMatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "AI Job Matching", description = "AI-powered candidate matching using vector similarity search")
public class JobMatchController {

    private final JobMatchingService jobMatchingService;

    @GetMapping("/{jobId}/matches")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Find matching candidates", description = "Uses vector similarity search and hybrid scoring (skill overlap + experience alignment) to find the best candidates for a job")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matched candidates with scores"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<List<MatchedCandidateDTO>> getMatchedCandidates(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "10") int top,
            org.springframework.security.core.Authentication authentication) {

        List<MatchedCandidateDTO> matches = jobMatchingService.getMatchedCandidates(jobId, top,
                authentication.getName());
        return ResponseEntity.ok(matches);
    }
}
