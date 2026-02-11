package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.entity.Job;
import com.codingshuttle.hackathon.skillsyncai.entity.User;
import com.codingshuttle.hackathon.skillsyncai.enums.EmploymentType;
import com.codingshuttle.hackathon.skillsyncai.enums.JobType;

import java.math.BigDecimal;
import java.util.List;

public interface JobService {
        Job createJob(Job job);

        Job getJob(Long id);

        List<Job> getAllJobs();

        List<Job> getJobsByRecruiter(User recruiter);

        Job updateJob(Long id, Job updatedDetails);

        void deleteJob(Long id);

        List<Job> searchJobs(String query);

        List<Job> filterJobs(JobType jobType, EmploymentType employmentType, String location,
                        BigDecimal minSalary, BigDecimal maxSalary, String skill);
}
