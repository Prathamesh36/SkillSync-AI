package com.codingshuttle.hackathon.skillsyncai.controller;

import com.codingshuttle.hackathon.skillsyncai.dto.ParsedResumeDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.Candidate;
import com.codingshuttle.hackathon.skillsyncai.entity.Resume;
import com.codingshuttle.hackathon.skillsyncai.entity.User;
import com.codingshuttle.hackathon.skillsyncai.enums.UserRole;
import com.codingshuttle.hackathon.skillsyncai.exception.BadRequestException;
import com.codingshuttle.hackathon.skillsyncai.exception.ResourceNotFoundException;
import com.codingshuttle.hackathon.skillsyncai.repository.CandidateRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.ResumeRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.UserRepository;
import com.codingshuttle.hackathon.skillsyncai.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Slf4j
public class ResumeController {

    private final AIService aiService;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;

    @PostMapping("/upload")
    public ResponseEntity<ParsedResumeDTO> uploadResume(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("file") MultipartFile file) throws IOException {
        log.info("Received resume upload request for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can upload resumes.");
        }

        // Parse resume using AI
        log.info("Parsing resume file: {}", file.getOriginalFilename());
        InputStreamResource resource = new InputStreamResource(file.getInputStream());
        ParsedResumeDTO parsed = aiService.parseResume(resource);
        log.debug("Resume parsed successfully: {}", parsed);

        // Save resume record
        Resume resume = resumeRepository.findByUserId(userId).orElse(new Resume());
        resume.setUser(user);
        resume.setFileName(file.getOriginalFilename());
        resume.setFileType(file.getContentType());
        resume.setParsedContent(parsed.summary());
        resume.setExtractedSkills(parsed.skills());
        resumeRepository.save(resume);
        log.info("Resume saved for user: {}", userId);

        // Update candidate profile with extracted data
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + userId));

        boolean candidateUpdated = false;
        if (parsed.skills() != null && !parsed.skills().isEmpty()) {
            candidate.setSkills(parsed.skills());
            candidateUpdated = true;
        }
        if (parsed.experienceYears() != null) {
            candidate.setExperienceYears(parsed.experienceYears());
            candidateUpdated = true;
        }
        if (candidateUpdated) {
            candidateRepository.save(candidate);
            log.info("Candidate profile updated with resume data for user: {}", userId);
        }

        // Update user name if not set
        if (parsed.fullName() != null && user.getName() == null) {
            user.setName(parsed.fullName());
            userRepository.save(user);
        }

        return ResponseEntity.ok(parsed);
    }
}
