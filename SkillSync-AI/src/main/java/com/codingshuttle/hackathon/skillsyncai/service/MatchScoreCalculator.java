package com.codingshuttle.hackathon.skillsyncai.service;

import java.util.List;

public interface MatchScoreCalculator {
    double MIN_SKILL_OVERLAP = 0.2;

    double calculateScore(List<String> jobSkills, List<String> candidateSkills, Integer jobExp, Integer candidateExp);

    double calculateSkillOverlap(List<String> jobSkills, List<String> candidateSkills);

    double calculateExperienceScore(Integer jobExp, Integer candidateExp);
}
