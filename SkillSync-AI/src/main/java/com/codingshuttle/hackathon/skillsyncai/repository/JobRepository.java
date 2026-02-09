package com.codingshuttle.hackathon.skillsyncai.repository;

import com.codingshuttle.hackathon.skillsyncai.entity.Job;
import com.codingshuttle.hackathon.skillsyncai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface JobRepository extends JpaSpecificationExecutor<Job>, JpaRepository<Job, Long> {
    List<Job> findByPostedBy(User user);

    long countByPostedByAndActiveTrue(User user);
}
