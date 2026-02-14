package com.codingshuttle.hackathon.skillsyncai.mapper;

import com.codingshuttle.hackathon.skillsyncai.dto.JobCreateDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.JobResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobMapper {

    public Job toEntity(JobCreateDTO dto) {
        Job job = new Job();
        job.setTitle(dto.title());
        job.setDescription(dto.description());
        // Company Name is set by Controller from Recruiter Profile
        job.setLocation(dto.location());
        job.setSalaryMin(dto.salaryMin());
        job.setSalaryMax(dto.salaryMax());
        job.setCurrency(dto.currency());
        job.setJobType(dto.jobType());
        job.setEmploymentType(dto.employmentType());
        job.setMinExperienceYears(dto.minExperienceYears());
        job.setMaxExperienceYears(dto.maxExperienceYears());
        job.setSkillsRequired(dto.skillsRequired());
        job.setApplicationDeadline(dto.applicationDeadline());
        job.setJobReferenceId(dto.jobReferenceId());
        return job;
    }

    public JobResponseDTO toDTO(Job job) {
        JobResponseDTO dto = new JobResponseDTO(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getCompanyName(),
                job.getLocation(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getCurrency(),
                job.getJobType(),
                job.getEmploymentType(),
                job.getPostedBy() != null ? job.getPostedBy().getId() : null,
                job.getMinExperienceYears(),
                job.getMaxExperienceYears(),
                job.getSkillsRequired(),
                job.getApplicationDeadline(),
                job.isActive(),
                job.getCreatedAt(),
                job.getJobReferenceId());
        return dto;
    }

    public void updateEntityFromDTO(JobCreateDTO dto, Job job) {
        if (dto.title() != null)
            job.setTitle(dto.title());
        if (dto.description() != null)
            job.setDescription(dto.description());
        if (dto.location() != null)
            job.setLocation(dto.location());
        if (dto.salaryMin() != null)
            job.setSalaryMin(dto.salaryMin());
        if (dto.salaryMax() != null)
            job.setSalaryMax(dto.salaryMax());
        if (dto.currency() != null)
            job.setCurrency(dto.currency());
        if (dto.jobType() != null)
            job.setJobType(dto.jobType());
        if (dto.employmentType() != null)
            job.setEmploymentType(dto.employmentType());
        if (dto.minExperienceYears() != null)
            job.setMinExperienceYears(dto.minExperienceYears());
        if (dto.maxExperienceYears() != null)
            job.setMaxExperienceYears(dto.maxExperienceYears());
        if (dto.skillsRequired() != null)
            job.setSkillsRequired(dto.skillsRequired());
        if (dto.applicationDeadline() != null)
            job.setApplicationDeadline(dto.applicationDeadline());
        if (dto.jobReferenceId() != null)
            job.setJobReferenceId(dto.jobReferenceId());
    }
}
