package com.codingshuttle.hackathon.skillsyncai.repository;

import com.codingshuttle.hackathon.skillsyncai.entity.InterviewSession;
import com.codingshuttle.hackathon.skillsyncai.enums.InterviewSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, UUID> {

    List<InterviewSession> findByCandidateId(Long candidateId);

    List<InterviewSession> findByCandidateIdAndStatus(Long candidateId, InterviewSessionStatus status);

    Optional<InterviewSession> findByIdAndCandidateId(UUID sessionId, Long candidateId);
}
