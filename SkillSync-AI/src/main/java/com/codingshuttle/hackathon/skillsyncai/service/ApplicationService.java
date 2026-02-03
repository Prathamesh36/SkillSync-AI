package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.ApplicationResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.Application;
import com.codingshuttle.hackathon.skillsyncai.entity.Job;
import com.codingshuttle.hackathon.skillsyncai.entity.Resume;
import com.codingshuttle.hackathon.skillsyncai.entity.User;
import com.codingshuttle.hackathon.skillsyncai.enums.ApplicationStatus;
import com.codingshuttle.hackathon.skillsyncai.mapper.ApplicationMapper;
import com.codingshuttle.hackathon.skillsyncai.repository.ApplicationRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.JobRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.ResumeRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final ChatClient.Builder chatClientBuilder;
    private final ApplicationMapper applicationMapper;

    @Transactional
    public ApplicationResponseDTO applyForJob(Long userId, Long jobId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        Resume resume = resumeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Resume not found. Please upload a resume first."));

        // Calculate Match Score using AI
        String prompt = String.format("""
                Analyze the match between job and candidate.

                Job:
                Title: %s
                Description: %s
                Required Skills: %s
                Experience Required: %d years

                Candidate:
                Skills: %s
                Experience: %d years
                Resume: %s

                Respond with ONLY a JSON object in this exact format:
                {"score": <number 0-100>, "analysis": "<brief explanation>"}
                """,
                job.getTitle(), job.getDescription(), String.join(", ", job.getSkillsRequired()),
                job.getRequiredExperienceYears() != null ? job.getRequiredExperienceYears() : 0,
                String.join(", ", user.getSkills() != null ? user.getSkills() : List.of()),
                user.getExperienceYears() != null ? user.getExperienceYears() : 0,
                resume.getParsedContent() != null
                        ? resume.getParsedContent().substring(0, Math.min(500, resume.getParsedContent().length()))
                        : "N/A");

        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt().user(prompt).call().content();

        // Parse simple JSON response
        Double matchScore = 50.0;
        String aiAnalysis = "Analysis pending";
        try {
            if (response.contains("score")) {
                String scoreStr = response.replaceAll(".*\"score\"\\s*:\\s*(\\d+\\.?\\d*).*", "$1");
                matchScore = Double.parseDouble(scoreStr);
            }
            if (response.contains("analysis")) {
                aiAnalysis = response.replaceAll(".*\"analysis\"\\s*:\\s*\"([^\"]+)\".*", "$1");
            }
        } catch (Exception e) {
            log.warn("Failed to parse AI response: {}", response, e);
        }

        Application application = new Application();
        application.setUser(user);
        application.setJob(job);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setMatchScore(matchScore);
        application.setAiAnalysis(aiAnalysis);

        Application saved = applicationRepository.save(application);
        return applicationMapper.toDTO(saved);
    }

    public List<ApplicationResponseDTO> getApplicationsByUser(Long userId) {
        return applicationRepository.findByUserId(userId).stream()
                .map(applicationMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ApplicationResponseDTO> getApplicationsForJob(Long jobId) {
        return applicationRepository.findByJobId(jobId).stream()
                .map(applicationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ApplicationResponseDTO updateStatus(Long applicationId, ApplicationStatus status) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        application.setStatus(status);
        return applicationMapper.toDTO(applicationRepository.save(application));
    }
}
