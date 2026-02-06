package com.codingshuttle.hackathon.skillsyncai.dto;

import java.util.List;

public record CandidateProfileDTO(
                List<String> skills,
                Integer experienceYears,
                String headline,
                String location,
                Long resumeId) {
}
