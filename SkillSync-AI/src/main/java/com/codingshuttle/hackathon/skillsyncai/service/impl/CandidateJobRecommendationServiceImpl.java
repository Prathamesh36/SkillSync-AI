package com.codingshuttle.hackathon.skillsyncai.service.impl;

import com.codingshuttle.hackathon.skillsyncai.dto.RecommendedJobResponse;
import com.codingshuttle.hackathon.skillsyncai.entity.*;
import com.codingshuttle.hackathon.skillsyncai.enums.JobInvitationStatus;
import com.codingshuttle.hackathon.skillsyncai.exception.ResourceNotFoundException;
import com.codingshuttle.hackathon.skillsyncai.repository.*;
import com.codingshuttle.hackathon.skillsyncai.service.CandidateJobRecommendationService;
import com.codingshuttle.hackathon.skillsyncai.service.MatchScoreCalculator;
import com.codingshuttle.hackathon.skillsyncai.service.VectorSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateJobRecommendationServiceImpl implements CandidateJobRecommendationService {

    private final VectorSearchService vectorSearchService;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final ResumeRepository resumeRepository;
    private final ApplicationRepository applicationRepository;
    private final JobInvitationRepository jobInvitationRepository;
    private final MatchResultRepository matchResultRepository;
    private final ChatClient.Builder chatClientBuilder;
    private final MatchScoreCalculator matchScoreCalculator;

    @Override
    public List<RecommendedJobResponse> getRecommendations(User user, int topK, Double minScore,
            String locationFilter) {
        log.info("Generating job recommendations for user: {}", user.getEmail());

        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        Resume resume = resumeRepository.findFirstByUserIdOrderByIdDesc(user.getId()).orElse(null);

        StringBuilder queryBuilder = new StringBuilder();
        if (resume != null && resume.getParsedContent() != null && !resume.getParsedContent().isEmpty()) {
            queryBuilder.append(resume.getParsedContent()).append(" ");
        }
        if (candidate.getHeadline() != null && !candidate.getHeadline().isEmpty()) {
            queryBuilder.append("Role: ").append(candidate.getHeadline()).append(". ");
        }
        if (candidate.getSkills() != null && !candidate.getSkills().isEmpty()) {
            queryBuilder.append("Skills: ").append(String.join(", ", candidate.getSkills())).append(". ");
        }
        if (user.getBio() != null && !user.getBio().isEmpty()) {
            queryBuilder.append("Background: ").append(user.getBio()).append(". ");
        }
        if (candidate.getExperienceYears() != null) {
            queryBuilder.append("Experience: ").append(candidate.getExperienceYears()).append(" years. ");
        }
        if (candidate.getLocation() != null && !candidate.getLocation().isEmpty()) {
            queryBuilder.append("Location: ").append(candidate.getLocation()).append(". ");
        }

        String queryText = queryBuilder.toString().trim();
        if (queryText.isEmpty())
            return Collections.emptyList();

        List<Document> similarDocs;
        try {
            similarDocs = vectorSearchService.findSimilarJobs(queryText, topK * 3);
        } catch (Exception e) {
            log.error("Vector search failed for user: {}", user.getEmail(), e);
            return Collections.emptyList();
        }

        if (similarDocs.isEmpty())
            return Collections.emptyList();

        Map<Long, Double> semanticScores = new LinkedHashMap<>();
        for (Document doc : similarDocs) {
            Long jobId = Long.valueOf(doc.getMetadata().get("jobId").toString());
            if (semanticScores.containsKey(jobId))
                continue;
            semanticScores.put(jobId, doc.getScore() != null ? doc.getScore() : 0.7);
        }

        List<Job> candidateJobs = jobRepository.findAllById(semanticScores.keySet());
        Map<Long, Job> jobMap = candidateJobs.stream().collect(Collectors.toMap(Job::getId, job -> job));

        List<RecommendedJobResponse> recommendations = new ArrayList<>();
        List<String> candidateSkills = candidate.getSkills() != null
                ? candidate.getSkills().stream().map(String::toLowerCase).toList()
                : (resume != null && resume.getExtractedSkills() != null
                        ? resume.getExtractedSkills().stream().map(String::toLowerCase).toList()
                        : Collections.emptyList());

        for (Document doc : similarDocs) {
            Long jobId = Long.valueOf(doc.getMetadata().get("jobId").toString());
            Job job = jobMap.get(jobId);
            if (job == null || !job.isActive())
                continue;

            if (job.getMinExperienceYears() != null && candidate.getExperienceYears() != null) {
                if (candidate.getExperienceYears() < job.getMinExperienceYears())
                    continue;
            }

            if (locationFilter != null && !locationFilter.isEmpty()) {
                if (!job.getLocation().toLowerCase().contains(locationFilter.toLowerCase()))
                    continue;
            }

            String appStatus = "APPLY_NOW";
            if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidate.getId())) {
                appStatus = "APPLIED";
            } else if (jobInvitationRepository.existsByJobIdAndCandidateIdAndStatus(jobId, candidate.getId(),
                    JobInvitationStatus.SENT)) {
                appStatus = "INVITED";
            }

            double skillScore = matchScoreCalculator.calculateSkillOverlap(job.getSkillsRequired(), candidateSkills);
            if (skillScore < MatchScoreCalculator.MIN_SKILL_OVERLAP)
                continue;

            double finalScore = matchScoreCalculator.calculateScore(job.getSkillsRequired(), candidateSkills,
                    job.getMinExperienceYears(), job.getMaxExperienceYears(), candidate.getExperienceYears());
            double matchPercentage = finalScore * 100;

            if (matchPercentage < (minScore != null ? minScore * 100 : 70.0))
                continue;

            recommendations.add(new RecommendedJobResponse(
                    job.getId(),
                    job.getTitle(),
                    job.getCompanyName(),
                    job.getLocation(),
                    job.getSkillsRequired(),
                    job.getSalaryMin(),
                    job.getSalaryMax(),
                    job.getJobType(),
                    job.getEmploymentType(),
                    appStatus,
                    matchPercentage,
                    null,
                    job.getMinExperienceYears(),
                    job.getMaxExperienceYears(),
                    job.getCreatedAt(),
                    job.getCurrency()));
        }

        recommendations.sort(Comparator.comparingDouble(RecommendedJobResponse::matchScore).reversed());
        if (recommendations.size() > topK)
            recommendations = recommendations.subList(0, topK);

        List<RecommendedJobResponse> finalRecommendations = new ArrayList<>();
        ChatClient chatClient = chatClientBuilder.build();
        for (RecommendedJobResponse rec : recommendations) {
            String explanation = null;
            try {
                Optional<MatchResult> cachedResult = matchResultRepository.findByJobIdAndCandidateId(rec.jobId(),
                        candidate.getId());
                if (cachedResult.isPresent() && cachedResult.get().getCandidateExplanation() != null) {
                    explanation = cachedResult.get().getCandidateExplanation();
                } else {
                    Job job = jobMap.get(rec.jobId());
                    explanation = generateAiExplanation(chatClient, candidate, job);
                    MatchResult result = cachedResult
                            .orElse(MatchResult.builder().jobId(rec.jobId()).candidateId(candidate.getId()).build());
                    result.setMatchScore(rec.matchScore());
                    result.setCandidateExplanation(explanation);
                    matchResultRepository.save(result);
                }
            } catch (Exception e) {
                log.error("Failed to generate explanation for job {}", rec.jobId(), e);
                explanation = "AI explanation unavailable at the moment.";
            }
            finalRecommendations.add(new RecommendedJobResponse(
                    rec.jobId(),
                    rec.title(),
                    rec.companyName(),
                    rec.location(),
                    rec.skillsRequired(),
                    rec.salaryMin(),
                    rec.salaryMax(),
                    rec.jobType(),
                    rec.employmentType(),
                    rec.applicationStatus(),
                    rec.matchScore(),
                    explanation,
                    rec.minExperienceYears(),
                    rec.maxExperienceYears(),
                    rec.createdAt(),
                    rec.currency()));
        }
        return finalRecommendations;
    }

    @Override
    public String getExplanation(Long userId, Long jobId) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));
        Optional<MatchResult> cachedResult = matchResultRepository.findByJobIdAndCandidateId(jobId, candidate.getId());
        if (cachedResult.isPresent() && cachedResult.get().getCandidateExplanation() != null
                && !cachedResult.get().getCandidateExplanation().isEmpty()) {
            return cachedResult.get().getCandidateExplanation();
        }
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        String explanation = generateAiExplanation(chatClientBuilder.build(), candidate, job);
        MatchResult result = cachedResult
                .orElse(MatchResult.builder().jobId(jobId).candidateId(candidate.getId()).build());
        result.setCandidateExplanation(explanation);
        matchResultRepository.save(result);
        return explanation;
    }

    private String generateAiExplanation(ChatClient chatClient, Candidate candidate, Job job) {
        List<String> jobSkills = job.getSkillsRequired() != null ? job.getSkillsRequired() : Collections.emptyList();
        List<String> candidateSkills = candidate.getSkills() != null ? candidate.getSkills() : Collections.emptyList();
        Set<String> candidateSkillSet = candidateSkills.stream().map(String::toLowerCase).collect(Collectors.toSet());
        List<String> missingSkills = jobSkills.stream()
                .filter(skill -> !candidateSkillSet.contains(skill.toLowerCase())).collect(Collectors.toList());

        String prompt = String.format("""
                You are a helpful career advisor speaking directly to the candidate %s.
                Analyze why this job at %s is a match, but also highlight any gaps.
                Candidate Skills: %s. Candidate Experience: %d years.
                Job Required Skills: %s. Job Required Experience: %d-%d years.
                Missing Skills: %s.
                Rules:
                1. Use "You" and "Your" to address the candidate.
                2. Write exactly 3 sentences:
                   - Sentence 1 & 2: Enthusiastically explain why they are a good match.
                   - Sentence 3: Gently mention what is missing or could be improved.
                3. Total length: 50-75 words.
                """,
                candidate.getUser().getName(), job.getCompanyName(), String.join(", ", candidateSkills),
                candidate.getExperienceYears(), String.join(", ", jobSkills),
                job.getMinExperienceYears() != null ? job.getMinExperienceYears() : 0,
                job.getMaxExperienceYears() != null ? job.getMaxExperienceYears() : 99,
                missingSkills.isEmpty() ? "None" : String.join(", ", missingSkills));
        return chatClient.prompt().user(prompt).call().content();
    }
}
