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
import com.codingshuttle.hackathon.skillsyncai.repository.ApplicationRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.MatchResultRepository;
import com.codingshuttle.hackathon.skillsyncai.service.AIService;
import com.codingshuttle.hackathon.skillsyncai.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Resumes", description = "Resume upload, AI parsing, embedding storage, and download")
public class ResumeController {

    private final AIService aiService;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final ApplicationRepository applicationRepository;
    private final MatchResultRepository matchResultRepository;
    private final StorageService storageService;

    @PostMapping("/upload")
    @Operation(summary = "Upload and parse resume", description = "Uploads a PDF/DOCX resume (to MinIO), parses it with AI, updates the candidate profile, and stores a vector embedding.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resume parsed and stored"),
            @ApiResponse(responseCode = "400", description = "Invalid file or non-candidate user")
    })
    public ResponseEntity<ParsedResumeDTO> uploadResume(
            org.springframework.security.core.Authentication authentication,
            @RequestParam("file") MultipartFile file) throws IOException {

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        Long userId = user.getId();
        log.info("Received resume upload request for user: {} ({})", userId, email);

        if (user.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can upload resumes.");
        }

        // 1. Upload to StorageService (MinIO)
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String storageFileName = storageService.uploadFile(file, fileName);
        log.info("File uploaded to storage: {}", storageFileName);

        // 2. Parse resume using the uploaded file (Download back for parsing)
        // We need a Resource to pass to AIService
        Resource storageResource = storageService.downloadFile(storageFileName);
        ParsedResumeDTO parsed;
        try {
            log.info("Parsing resume file...");
            parsed = aiService.parseResume(storageResource);
            log.debug("Resume parsed successfully: {}", parsed);
        } catch (Exception e) {
            log.error("Failed to parse resume with AI: {}. Proceeding with upload only.", e.getMessage());
            // Create empty parsed resume DTO with null/empty values
            parsed = new ParsedResumeDTO(null, null, java.util.Collections.emptyList(), null, null, null);
        }

        // 3. Save/Update resume record
        Resume resume = resumeRepository.findByUserId(userId).orElse(new Resume());
        resume.setUser(user);
        resume.setFileName(file.getOriginalFilename());
        resume.setFileType(file.getContentType());
        resume.setS3Url(storageFileName); // Storing the MinIO object name
        resume.setParsedContent(parsed.summary());
        resume.setExtractedSkills(parsed.skills());
        resumeRepository.save(resume);
        log.info("Resume saved for user: {}", userId);

        // 4. Update candidate profile with extracted data
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

        // Invalidate Cache for Recommendations
        matchResultRepository.deleteByCandidateId(candidate.getId());
        log.info("Invalidated recommendation cache for candidate: {}", candidate.getId());

        // 5. Update user name if not set
        if (parsed.fullName() != null && user.getName() == null) {
            user.setName(parsed.fullName());
            userRepository.save(user);
        }

        // 6. Generate and Store Embedding
        try {
            String resumeContent = "Candidate Name: " + (parsed.fullName() != null ? parsed.fullName() : "N/A") +
                    "\nSkills: " + (parsed.skills() != null ? String.join(", ", parsed.skills()) : "N/A") +
                    "\nExperience: " + (parsed.experienceYears() != null ? parsed.experienceYears() : 0) + " years" +
                    "\nSummary: " + (parsed.summary() != null ? parsed.summary() : "N/A") +
                    "\nEducation: " + (parsed.education() != null ? parsed.education() : "N/A");

            java.util.Map<String, Object> metadata = new java.util.HashMap<>();
            metadata.put("userId", userId);
            metadata.put("docType", "RESUME");

            aiService.storeResumeEmbedding(resume.getId(), resumeContent, metadata);
            log.info("Resume embedding stored for resumeId: {}", resume.getId());
        } catch (Exception e) {
            log.error("Failed to store resume embedding for user: {}", userId, e);
        }

        return ResponseEntity.ok(parsed);
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete current user's resume", description = "Deletes the resume file from MinIO and record. Blocked if resume is linked to active applications.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Resume deleted"),
            @ApiResponse(responseCode = "400", description = "Resume in use by active applications"),
            @ApiResponse(responseCode = "404", description = "Resume not found")
    })
    public ResponseEntity<Void> deleteResume(org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Long userId = user.getId();

        Resume resume = resumeRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        // Check for active applications
        if (applicationRepository.existsByResumeId(resume.getId())) {
            throw new BadRequestException(
                    "Cannot delete resume as it is used in job applications. Please upload a new resume to replace it for future applications.");
        }

        // Delete file from Storage
        try {
            if (resume.getS3Url() != null) {
                storageService.deleteFile(resume.getS3Url());
            }
        } catch (IOException e) {
            log.warn("Failed to delete resume file from storage: {}", resume.getS3Url());
        }

        // Delete record
        resumeRepository.delete(resume);

        log.info("Resume deleted for user: {}", userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download/{resumeId}")
    @Operation(summary = "Download a resume file", description = "Returns the resume file as an attachment from MinIO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File returned"),
            @ApiResponse(responseCode = "404", description = "Resume not found")
    })
    public ResponseEntity<Resource> downloadResume(@PathVariable Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));

        if (resume.getS3Url() == null) {
            throw new ResourceNotFoundException("Resume file info not found");
        }

        try {
            Resource resource = storageService.downloadFile(resume.getS3Url());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(
                            resume.getFileType() != null ? resume.getFileType() : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resume.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Failed to download file: {}", resume.getS3Url(), e);
            throw new ResourceNotFoundException(
                    "Could not download file: " + resume.getFileName());
        }
    }
}
