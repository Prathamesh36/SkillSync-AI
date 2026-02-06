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
        job.setCompanyName(dto.companyName());
        job.setLocation(dto.location());
        job.setSalaryMin(dto.salaryMin());
        job.setSalaryMax(dto.salaryMax());
        job.setCurrency(dto.currency());
        job.setJobType(dto.jobType());
        job.setEmploymentType(dto.employmentType());
        job.setRequiredExperienceYears(dto.requiredExperienceYears());
        job.setSkillsRequired(dto.skillsRequired());
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
                job.getRequiredExperienceYears(),
                job.getSkillsRequired(),
                job.getPostedBy() != null ? job.getPostedBy().getId() : null,
                job.isActive(),
                job.getCreatedAt());
        return dto;
    }

    public void updateEntityFromDTO(JobCreateDTO dto, Job job) {
        if (dto.title() != null)
            job.setTitle(dto.title());
        if (dto.description() != null)
            job.setDescription(dto.description());
        if (dto.companyName() != null)
            job.setCompanyName(dto.companyName());
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
        if (dto.requiredExperienceYears() != null)
            job.setRequiredExperienceYears(dto.requiredExperienceYears());
        if (dto.skillsRequired() != null)
            job.setSkillsRequired(dto.skillsRequired());
    }
}
