package com.codingshuttle.hackathon.skillsyncai.controller;

import com.codingshuttle.hackathon.skillsyncai.dto.RecommendedJobResponse;
import com.codingshuttle.hackathon.skillsyncai.entity.User;
import com.codingshuttle.hackathon.skillsyncai.exception.ResourceNotFoundException;
import com.codingshuttle.hackathon.skillsyncai.repository.UserRepository;
import com.codingshuttle.hackathon.skillsyncai.service.CandidateJobRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidates/me/recommended-jobs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Job Recommendations", description = "AI-powered job recommendations for candidates based on resume embeddings")
public class CandidateJobRecommendationController {

        private final CandidateJobRecommendationService recommendationService;
        private final UserRepository userRepository;

        @GetMapping
        @PreAuthorize("hasAnyRole('CANDIDATE')")
        @Operation(summary = "Get recommended jobs", description = "Returns AI-powered job recommendations using vector similarity search on resume embeddings. Filterable by score threshold, top-N, and location.")
        @ApiResponse(responseCode = "200", description = "List of recommended jobs with match scores")
        public ResponseEntity<List<RecommendedJobResponse>> getRecommendedJobs(
                        Authentication authentication,
                        @RequestParam(defaultValue = "5") int top,
                        @RequestParam(defaultValue = "0.6") Double minScore,
                        @RequestParam(required = false) String location) {

                String email = authentication.getName();
                log.info("Fetching recommended jobs for candidate: {}", email);

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                List<RecommendedJobResponse> recommendations = recommendationService.getRecommendations(user, top,
                                minScore, location);

                return ResponseEntity.ok(recommendations);
        }

        @GetMapping("/{jobId}/explanation")
        @PreAuthorize("hasAnyRole('CANDIDATE')")
        @Operation(summary = "Get recommendation explanation", description = "Returns an AI-generated natural language explanation of why a specific job was recommended")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Explanation text"),
                        @ApiResponse(responseCode = "404", description = "Job or user not found")
        })
        public ResponseEntity<String> getExplanation(
                        Authentication authentication,
                        @PathVariable Long jobId) {

                String email = authentication.getName();
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                String explanation = recommendationService.getExplanation(user.getId(), jobId);

                return ResponseEntity.ok(explanation);
        }
}
