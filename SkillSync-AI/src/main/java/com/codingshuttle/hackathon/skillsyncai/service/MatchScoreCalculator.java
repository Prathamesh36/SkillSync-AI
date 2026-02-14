package com.codingshuttle.hackathon.skillsyncai.service;

import java.util.List;

public interface MatchScoreCalculator {
    double MIN_SKILL_OVERLAP = 0.3;

    double calculateScore(List<String> jobSkills, List<String> candidateSkills, Integer minExp, Integer maxExp,
            Integer candidateExp);

    double calculateSkillOverlap(List<String> jobSkills, List<String> candidateSkills);

    double calculateExperienceScore(Integer minExp, Integer maxExp, Integer candidateExp);
}
