package com.codingshuttle.hackathon.skillsyncai.repository;

import com.codingshuttle.hackathon.skillsyncai.entity.InterviewTranscript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewTranscriptRepository extends JpaRepository<InterviewTranscript, Long> {

    Optional<InterviewTranscript> findByInterviewSessionId(UUID sessionId);
}
