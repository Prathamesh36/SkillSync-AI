package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.InvitationAcceptResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.InvitationResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.InviteCandidateRequestDTO;

import java.util.List;

public interface JobInvitationService {
        InvitationResponseDTO inviteCandidate(Long jobId, InviteCandidateRequestDTO request, String recruiterEmail);

        List<InvitationResponseDTO> getCandidateInvitations(String candidateEmail);

        InvitationAcceptResponseDTO acceptInvitation(String token, String candidateEmail);

        InvitationResponseDTO declineInvitation(String token, String candidateEmail);
}
