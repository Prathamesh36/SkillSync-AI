package com.codingshuttle.hackathon.skillsyncai.repository;

import com.codingshuttle.hackathon.skillsyncai.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
    Optional<MatchResult> findByJobIdAndCandidateId(Long jobId, Long candidateId);

    List<MatchResult> findByJobId(Long jobId);

    @org.springframework.transaction.annotation.Transactional
    void deleteByCandidateId(Long candidateId);
}
