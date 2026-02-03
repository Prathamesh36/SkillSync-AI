package com.codingshuttle.hackathon.skillsyncai.controller;

import com.codingshuttle.hackathon.skillsyncai.dto.ParsedResumeDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.Resume;
import com.codingshuttle.hackathon.skillsyncai.entity.User;
import com.codingshuttle.hackathon.skillsyncai.repository.ResumeRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.UserRepository;
import com.codingshuttle.hackathon.skillsyncai.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final AIService aiService;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    @PostMapping("/upload")
    public ResponseEntity<ParsedResumeDTO> uploadResume(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("file") MultipartFile file) throws IOException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Parse resume using AI
        InputStreamResource resource = new InputStreamResource(file.getInputStream());
        ParsedResumeDTO parsed = aiService.parseResume(resource);

        // Save resume record
        Resume resume = resumeRepository.findByUserId(userId).orElse(new Resume());
        resume.setUser(user);
        resume.setFileName(file.getOriginalFilename());
        resume.setFileType(file.getContentType());
        resume.setParsedContent(parsed.summary());
        resume.setExtractedSkills(parsed.skills());
        resumeRepository.save(resume);

        // Update user profile with extracted data
        if (parsed.skills() != null && !parsed.skills().isEmpty()) {
            user.setSkills(parsed.skills());
        }
        if (parsed.experienceYears() != null) {
            user.setExperienceYears(parsed.experienceYears());
        }
        if (parsed.fullName() != null && user.getName() == null) {
            user.setName(parsed.fullName());
        }
        userRepository.save(user);

        return ResponseEntity.ok(parsed);
    }
}
