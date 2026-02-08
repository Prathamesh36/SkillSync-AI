package com.codingshuttle.hackathon.skillsyncai.repository;

import com.codingshuttle.hackathon.skillsyncai.entity.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    Optional<Candidate> findByUserId(Long userId);

    java.util.List<Candidate> findByUser_IdIn(java.util.Collection<Long> userIds);

    @Query("SELECT c FROM Candidate c LEFT JOIN FETCH c.skills WHERE c.id = :id")
    Optional<Candidate> findByIdWithSkills(@Param("id") Long id);
}
