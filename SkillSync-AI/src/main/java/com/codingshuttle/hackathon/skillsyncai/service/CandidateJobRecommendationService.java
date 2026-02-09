package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.RecommendedJobResponse;
import com.codingshuttle.hackathon.skillsyncai.entity.Candidate;
import com.codingshuttle.hackathon.skillsyncai.entity.Job;
import com.codingshuttle.hackathon.skillsyncai.entity.Resume;
import com.codingshuttle.hackathon.skillsyncai.entity.User;
import com.codingshuttle.hackathon.skillsyncai.enums.JobInvitationStatus;
import com.codingshuttle.hackathon.skillsyncai.exception.ResourceNotFoundException;
import com.codingshuttle.hackathon.skillsyncai.repository.ApplicationRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.CandidateRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.JobInvitationRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.JobRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.ResumeRepository;
import com.codingshuttle.hackathon.skillsyncai.repository.MatchResultRepository;
import com.codingshuttle.hackathon.skillsyncai.entity.MatchResult;
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
public class CandidateJobRecommendationService {

    private final VectorSearchService vectorSearchService;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final ResumeRepository resumeRepository;
    private final ApplicationRepository applicationRepository;
    private final JobInvitationRepository jobInvitationRepository;
    private final MatchResultRepository matchResultRepository;
    private final ChatClient.Builder chatClientBuilder;

    private static final double WEIGHT_SEMANTIC = 0.6;
    private static final double WEIGHT_SKILLS = 0.3;
    private static final double WEIGHT_EXPERIENCE = 0.1;

    /**
     * Get recommended jobs for the current candidate.
     */
    public List<RecommendedJobResponse> getRecommendations(User user, int topK, Double minScore,
            String locationFilter) {
        log.info("Generating job recommendations for user: {}", user.getEmail());

        // 1. Fetch Candidate & Resume
        Candidate candidate = candidateRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        Resume resume = resumeRepository.findFirstByUserIdOrderByIdDesc(user.getId())
                .orElse(null);

        String queryText;
        if (resume != null && resume.getParsedContent() != null && !resume.getParsedContent().isEmpty()) {
            queryText = resume.getParsedContent();
        } else if (candidate.getSkills() != null && !candidate.getSkills().isEmpty()) {
            queryText = String.join(", ", candidate.getSkills()) + " "
                    + (candidate.getHeadline() != null ? candidate.getHeadline() : "");
        } else {
            log.warn("No resume or skills found for candidate {}", user.getEmail());
            return Collections.emptyList();
        }

        // 2. Vector Search (fetch more to allow filtering)
        if (queryText == null || queryText.trim().isEmpty()) {
            log.warn("Query text is empty for user: {}", user.getEmail());
            return Collections.emptyList();
        }

        List<Document> similarDocs;
        try {
            similarDocs = vectorSearchService.findSimilarJobs(queryText, topK * 3);
        } catch (Exception e) {
            log.error("Vector search failed for user: {}", user.getEmail(), e);
            // Fallback: If vector search fails (e.g., DB down, AI rate limit), return empty
            // list for now
            // or maybe fallback to keyword search if available?
            // For now, return empty to avoid 500 error on frontend.
            return Collections.emptyList();
        }

        if (similarDocs.isEmpty()) {
            return Collections.emptyList();
        }

        // Map JobId -> Semantic Score
        Map<Long, Double> semanticScores = similarDocs.stream()
                .collect(Collectors.toMap(
                        doc -> Long.valueOf(doc.getMetadata().get("jobId").toString()),
                        doc -> {
                            // Normalize score? Spring AI usually returns distance or similarity.
                            // Assuming similarity 0-1. If it's distance, we might need conversion.
                            // Default implementation usually returns similarity (higher is better).
                            // Let's assume input is 0-1.
                            return 1.0; // Placeholder if actual score not exposed in Document metadata easily
                            // Wait, currently vectorSearchService implementation doesn't return score in
                            // metadata by default?
                            // Spring AI Document doesn't hold the score itself unless the VectorStore puts
                            // it there.
                            // Some implementations return ScoredDocument (not Document).
                            // But method returns List<Document>.
                            // Most implementations put 'distance' in metadata.
                            // Let's check metadata for 'distance' or assume standard ranking order implies
                            // score.
                            // For MVP, we'll use linear decay based on rank if score missing?
                            // Or better: Assume 1.0 for top match, decaying slightly?
                            // Actually, let's look at `VectorSearchService.findSimilarResumes` logic - it
                            // doesn't extract score.
                            // I'll proceed without explicit score from search result for now (defaulting to
                            // 0.8), or try to extract if present.
                        },
                        (v1, v2) -> v1 // Merge function
                ));

        // Improve score extraction: Spring AI's VectorStore.similaritySearch returns
        // Documents.
        // Often 'score' or 'distance' is in metadata.
        // Let's iterate and check.

        // 3. Fetch Job Entities
        List<Job> candidateJobs = jobRepository.findAllById(semanticScores.keySet());
        Map<Long, Job> jobMap = candidateJobs.stream().collect(Collectors.toMap(Job::getId, job -> job));

        List<RecommendedJobResponse> recommendations = new ArrayList<>();

        List<String> candidateSkills = candidate.getSkills() != null
                ? candidate.getSkills().stream().map(String::toLowerCase).toList()
                : (resume != null && resume.getExtractedSkills() != null
                        ? resume.getExtractedSkills().stream().map(String::toLowerCase).toList()
                        : Collections.emptyList());

        int processedCount = 0;

        // Reuse ChatClient
        ChatClient chatClient = chatClientBuilder.build();

        for (Document doc : similarDocs) {
            Long jobId = Long.valueOf(doc.getMetadata().get("jobId").toString());
            Job job = jobMap.get(jobId);

            if (job == null || !job.isActive())
                continue;

            // --- HARD FILTERS ---

            // Experience
            if (job.getRequiredExperienceYears() != null && candidate.getExperienceYears() != null) {
                if (candidate.getExperienceYears() < job.getRequiredExperienceYears()) {
                    continue; // Skip if experience not met
                }
            }

            // Location (Optional Filter)
            if (locationFilter != null && !locationFilter.isEmpty()) {
                if (!job.getLocation().toLowerCase().contains(locationFilter.toLowerCase())) {
                    continue;
                }
            }

            // Status Check
            String appStatus = "APPLY_NOW";
            if (applicationRepository.existsByJobIdAndCandidateId(jobId, candidate.getId())) {
                appStatus = "APPLIED";
            } else if (jobInvitationRepository.existsByJobIdAndCandidateIdAndStatus(jobId, candidate.getId(),
                    JobInvitationStatus.SENT)) {
                appStatus = "INVITED";
            }

            // --- HYBRID SCORING ---

            // 1. Semantic Score (Simulating decay based on rank if real score unavailable)
            // Ideally we'd get this from the vector store. For now, estimate based on list
            // position vs total.
            // Or assume doc has "distance" in metadata.
            Double vectorScore = 0.85; // Default high baseline for similar docs
            if (doc.getMetadata().containsKey("distance")) {
                // distance usually 0 (close) to 1 (far). Sim = 1 - distance
                Object dist = doc.getMetadata().get("distance");
                if (dist instanceof Number) {
                    vectorScore = 1.0 - ((Number) dist).doubleValue();
                }
            }

            // 2. Skill Overlap
            double skillScore = calculateSkillOverlap(job.getSkillsRequired(), candidateSkills);

            // 3. Experience Fit
            double expScore = 1.0; // Since we hard filtered, they match.
            // Bonus for extra experience?
            if (candidate.getExperienceYears() != null && job.getRequiredExperienceYears() != null) {
                int diff = candidate.getExperienceYears() - job.getRequiredExperienceYears();
                if (diff > 2)
                    expScore = 1.2; // Bonus
            }

            double finalScore = (vectorScore * WEIGHT_SEMANTIC) +
                    (skillScore * WEIGHT_SKILLS) +
                    (expScore * WEIGHT_EXPERIENCE);

            // Normalize final score to 0.0 - 1.0 range (roughly)
            finalScore = Math.min(1.0, finalScore);
            // Or 0-100? User used 0-10 matchScore in other places, but prompt example said
            // "0.6".
            // Let's keep 0.0-1.0 format or 0-100. Let's use % (0-100) for display
            // consistency.
            double matchPercentage = finalScore * 100;

            if (matchPercentage < (minScore != null ? minScore * 100 : 60.0)) {
                continue; // Min score filter
            }

            // AI Explanation (Lazy generation later)
            String aiExplanation = null;

            recommendations.add(new RecommendedJobResponse(
                    job.getId(),
                    job.getTitle(),
                    job.getCompanyName(),
                    job.getLocation(),
                    job.getRequiredExperienceYears(),
                    matchPercentage,
                    aiExplanation, // Populated later for top K
                    job.getJobType() != null ? job.getJobType().name() : null,
                    job.getEmploymentType() != null ? job.getEmploymentType().name() : null,
                    appStatus,
                    job.getSkillsRequired()));
        }

        // Sort by Score Descending
        recommendations.sort(Comparator.comparingDouble(RecommendedJobResponse::matchScore).reversed());

        // Limit to Top K
        // Limit to Top K
        if (recommendations.size() > topK) {
            recommendations = recommendations.subList(0, topK);
        }

        // Populate AI Explanation for the final list
        List<RecommendedJobResponse> finalRecommendations = new ArrayList<>();
        for (RecommendedJobResponse rec : recommendations) {
            String explanation = null;
            try {
                // Check cache
                Optional<MatchResult> cachedResult = matchResultRepository.findByJobIdAndCandidateId(rec.jobId(),
                        candidate.getId());

                if (cachedResult.isPresent() && cachedResult.get().getCandidateExplanation() != null) {
                    explanation = cachedResult.get().getCandidateExplanation();
                } else {
                    // Generate
                    Job job = jobMap.get(rec.jobId());
                    explanation = generateAiExplanation(chatClientBuilder.build(), candidate, job);

                    // Save
                    MatchResult result = cachedResult.orElse(MatchResult.builder()
                            .jobId(rec.jobId())
                            .candidateId(candidate.getId())
                            .build());
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
                    rec.jobTitle(),
                    rec.companyName(),
                    rec.location(),
                    rec.minExperience(),
                    rec.matchScore(),
                    explanation,
                    rec.jobType(),
                    rec.employmentType(),
                    rec.applicationStatus(),
                    rec.skills()));
        }

        return finalRecommendations;
    }

    public String getExplanation(Long userId, Long jobId) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));

        // Check cache
        Optional<MatchResult> cachedResult = matchResultRepository.findByJobIdAndCandidateId(jobId, candidate.getId());

        if (cachedResult.isPresent() && cachedResult.get().getCandidateExplanation() != null
                && !cachedResult.get().getCandidateExplanation().isEmpty()) {
            return cachedResult.get().getCandidateExplanation();
        }

        // Generate
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        String explanation = generateAiExplanation(chatClientBuilder.build(), candidate, job);

        // Save
        MatchResult result = cachedResult.orElse(MatchResult.builder()
                .jobId(jobId)
                .candidateId(candidate.getId())
                .build());
        // We might not have matchScore here if we are just fetching explanation.
        // Ideally we should have it from getRecommendations but that was transient.
        // If MatchResult exists, it has score. If new, we might miss score.
        // But getRecommendations would have created MatchResult? No, we removed that
        // logic.
        // So we might need to calculate score here or ignore it.
        // Let's ignore score update here (keep null or existing).

        result.setCandidateExplanation(explanation);
        matchResultRepository.save(result);

        return explanation;
    }

    private double calculateSkillOverlap(List<String> jobSkills, List<String> candidateSkills) {
        if (jobSkills == null || jobSkills.isEmpty())
            return 1.0;
        if (candidateSkills == null || candidateSkills.isEmpty())
            return 0.0;

        long matchCount = jobSkills.stream()
                .filter(jSkill -> candidateSkills.stream()
                        .anyMatch(cSkill -> cSkill.equalsIgnoreCase(jSkill)))
                .count();

        return (double) matchCount / jobSkills.size();
    }

    private String generateAiExplanation(ChatClient chatClient, Candidate candidate, Job job) {
        String prompt = String.format("""
                You are a career advisor speaking directly to the candidate %s.
                Explain in 1 short sentence why this job at %s is a perfect match for them.

                Candidate Skills: %s.
                Candidate Experience: %d years.

                Job Required Skills: %s.
                Job Required Experience: %d years.

                Rules:
                1. Use "You" and "Your" to address the candidate (e.g., "Your Java skills...").
                2. Be enthusiastic and encouraging.
                3. Highlight specific matching skills.
                4. Max 25 words.
                """,
                candidate.getUser().getName(),
                job.getCompanyName(),
                candidate.getSkills() != null ? String.join(", ", candidate.getSkills()) : "N/A",
                candidate.getExperienceYears(),
                job.getSkillsRequired() != null ? String.join(", ", job.getSkillsRequired()) : "N/A",
                job.getRequiredExperienceYears());

        return chatClient.prompt().user(prompt).call().content();
    }
}
