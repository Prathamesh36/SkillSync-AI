package com.codingshuttle.hackathon.skillsyncai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchedCandidateDTO {
    private Long candidateId;
    private Long resumeId;
    private String name;
    private String email;
    private Integer experienceYears;
    private List<String> skills;
    private String location;
    private Double matchScore;
    private String explanation;
    private String invitationStatus; // SENT, ACCEPTED, DECLINED, EXPIRED, null
}
