package com.codingshuttle.hackathon.skillsyncai.service.impl;

import com.codingshuttle.hackathon.skillsyncai.service.MatchScoreCalculator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchScoreCalculatorImpl implements MatchScoreCalculator {

    private static final double BASE_SCORE = 0.30;
    private static final double VARIABLE_WEIGHT = 0.70;
    private static final double WEIGHT_SKILLS = 0.75;
    private static final double WEIGHT_EXPERIENCE = 0.25;
    public static final double MIN_SKILL_OVERLAP = 0.2;

    @Override
    public double calculateScore(List<String> jobSkills, List<String> candidateSkills,
            Integer jobExp, Integer candidateExp) {
        double skillScore = calculateSkillOverlap(jobSkills, candidateSkills);
        double expScore = calculateExperienceScore(jobExp, candidateExp);

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
    public double calculateExperienceScore(Integer jobExp, Integer candidateExp) {
        if (jobExp == null || jobExp == 0)
            return 1.0;
        if (candidateExp == null)
            return 0.5;

        if (candidateExp >= jobExp) {
            return 1.0;
        }
        return Math.max(0.3, (double) candidateExp / jobExp);
    }
}
