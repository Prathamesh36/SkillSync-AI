package com.codingshuttle.hackathon.skillsyncai.mapper;

import com.codingshuttle.hackathon.skillsyncai.dto.JobCreateDTO;
import com.codingshuttle.hackathon.skillsyncai.dto.JobResponseDTO;
import com.codingshuttle.hackathon.skillsyncai.entity.Job;
import lombok.RequiredArgsConstructor;
// import org.modelmapper.ModelMapper; // Removed
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobMapper {

    // private final ModelMapper modelMapper; // Removed

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
}
