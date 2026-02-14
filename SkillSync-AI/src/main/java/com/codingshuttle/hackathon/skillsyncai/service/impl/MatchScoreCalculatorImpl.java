package com.codingshuttle.hackathon.skillsyncai.service.impl;

import com.codingshuttle.hackathon.skillsyncai.service.MatchScoreCalculator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchScoreCalculatorImpl implements MatchScoreCalculator {

    private static final double BASE_SCORE = 0.10;
    private static final double VARIABLE_WEIGHT = 0.90;
    private static final double WEIGHT_SKILLS = 0.75;
    private static final double WEIGHT_EXPERIENCE = 0.25;
    public static final double MIN_SKILL_OVERLAP = 0.3;

    @Override
    public double calculateScore(List<String> jobSkills, List<String> candidateSkills,
            Integer minExp, Integer maxExp, Integer candidateExp) {
        double skillScore = calculateSkillOverlap(jobSkills, candidateSkills);
        double expScore = calculateExperienceScore(minExp, maxExp, candidateExp);

        double variable = (WEIGHT_SKILLS * skillScore) + (WEIGHT_EXPERIENCE * expScore);
        double raw = BASE_SCORE + (VARIABLE_WEIGHT * variable);
        return Math.min(1.0, raw);
    }

    @Override
    public double calculateSkillOverlap(List<String> jobSkills, List<String> candidateSkills) {
        if (jobSkills == null || jobSkills.isEmpty())
            return 1.0;
        if (candidateSkills == null || candidateSkills.isEmpty())
            return 0.0;

        List<String> candidateLower = candidateSkills.stream()
                .map(String::toLowerCase)
                .toList();

        long matchCount = jobSkills.stream()
                .filter(jobSkill -> {
                    String jLower = jobSkill.toLowerCase();
                    return candidateLower.stream().anyMatch(cSkill -> cSkill.equals(jLower)
                            || cSkill.contains(jLower)
                            || jLower.contains(cSkill));
                })
                .count();

        return (double) matchCount / jobSkills.size();
    }

    @Override
    public double calculateExperienceScore(Integer minExp, Integer maxExp, Integer candidateExp) {
        // No experience requirement = perfect match
        if ((minExp == null || minExp == 0) && (maxExp == null || maxExp == 0))
            return 1.0;
        if (candidateExp == null)
            return 0.5;

        int effectiveMin = (minExp != null) ? minExp : 0;
        int effectiveMax = (maxExp != null) ? maxExp : Integer.MAX_VALUE;

        if (candidateExp >= effectiveMin && candidateExp <= effectiveMax) {
            // Within range — perfect match
            return 1.0;
        } else if (candidateExp > effectiveMax && maxExp != null) {
            // Overqualified — slight penalty
            return 0.85;
        } else {
            // Underqualified — proportional score
            return Math.max(0.3, (double) candidateExp / effectiveMin);
        }
    }
}
