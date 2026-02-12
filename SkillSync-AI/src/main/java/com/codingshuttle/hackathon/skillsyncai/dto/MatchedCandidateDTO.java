package com.codingshuttle.hackathon.skillsyncai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI-matched candidate with hybrid match score")
public class MatchedCandidateDTO {
    @Schema(description = "Candidate profile ID")
    private Long candidateId;
    @Schema(description = "Resume ID")
    private Long resumeId;
    @Schema(description = "Candidate name")
    private String name;
    @Schema(description = "Candidate email")
    private String email;
    @Schema(description = "Years of experience")
    private Integer experienceYears;
    @Schema(description = "Candidate skills")
    private List<String> skills;
    @Schema(description = "Candidate location")
    private String location;
    @Schema(description = "AI-computed match score (0.0 - 1.0)")
    private Double matchScore;
    @Schema(description = "AI explanation of match quality")
    private String explanation;
    @Schema(description = "Invitation status: SENT, ACCEPTED, DECLINED, EXPIRED, or null")
    private String invitationStatus;
}
