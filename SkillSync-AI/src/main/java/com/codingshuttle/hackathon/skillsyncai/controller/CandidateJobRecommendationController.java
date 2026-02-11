package com.codingshuttle.hackathon.skillsyncai.controller;

import com.codingshuttle.hackathon.skillsyncai.dto.RecommendedJobResponse;
import com.codingshuttle.hackathon.skillsyncai.entity.User;
import com.codingshuttle.hackathon.skillsyncai.exception.ResourceNotFoundException;
import com.codingshuttle.hackathon.skillsyncai.repository.UserRepository;
import com.codingshuttle.hackathon.skillsyncai.service.CandidateJobRecommendationService;
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
public class CandidateJobRecommendationController {

        private final CandidateJobRecommendationService recommendationService;
        private final UserRepository userRepository;

        @GetMapping
        @PreAuthorize("hasAnyRole('CANDIDATE')")
        public ResponseEntity<List<RecommendedJobResponse>> getRecommendedJobs(
                        Authentication authentication,
                        @RequestParam(defaultValue = "5") int top,
                        @RequestParam(defaultValue = "0.7") Double minScore,
                        @RequestParam(required = false) String location) {

                String email = authentication.getName();
                log.info("Fetching recommended jobs for candidate: {}", email);

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                List<RecommendedJobResponse> recommendations = recommendationService.getRecommendations(
                                user, top, minScore, location);

                return ResponseEntity.ok(recommendations);
        }

        @GetMapping("/{jobId}/explanation")
        @PreAuthorize("hasAnyRole('CANDIDATE')")
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
