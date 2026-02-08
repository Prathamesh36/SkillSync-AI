package com.codingshuttle.hackathon.skillsyncai.repository;

import com.codingshuttle.hackathon.skillsyncai.entity.InterviewSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewScheduleRepository extends JpaRepository<InterviewSchedule, Long> {

    /**
     * Find interview by application ID.
     */
    Optional<InterviewSchedule> findByJobApplicationId(Long applicationId);

    /**
     * Check if an interview already exists for an application.
     */
    boolean existsByJobApplicationId(Long applicationId);

    /**
     * Find all interviews for a candidate.
     */
    List<InterviewSchedule> findByCandidateId(Long candidateId);

    /**
     * Find all interviews for a specific job.
     */
    List<InterviewSchedule> findByJobApplicationJobId(Long jobId);

    /**
     * Find all interviews for a verified recruiter.
     */
    List<InterviewSchedule> findByRecruiterId(Long recruiterId);

    long countByRecruiterIdAndStatus(Long recruiterId,
            com.codingshuttle.hackathon.skillsyncai.enums.InterviewScheduleStatus status);
}
