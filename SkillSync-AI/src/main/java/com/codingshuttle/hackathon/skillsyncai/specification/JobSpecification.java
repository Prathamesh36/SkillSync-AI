package com.codingshuttle.hackathon.skillsyncai.specification;

import com.codingshuttle.hackathon.skillsyncai.entity.Job;
import com.codingshuttle.hackathon.skillsyncai.enums.EmploymentType;
import com.codingshuttle.hackathon.skillsyncai.enums.JobType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class JobSpecification {

    public static Specification<Job> filterJobs(
            JobType jobType,
            EmploymentType employmentType,
            String location,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            String skill) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by Active Jobs
            predicates.add(criteriaBuilder.isTrue(root.get("active")));

            if (jobType != null) {
                predicates.add(criteriaBuilder.equal(root.get("jobType"), jobType));
            }

            if (employmentType != null) {
                predicates.add(criteriaBuilder.equal(root.get("employmentType"), employmentType));
            }

            if (StringUtils.hasText(location)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("location")),
                        "%" + location.toLowerCase() + "%"));
            }

            if (minSalary != null) {
                // job.salaryMax >= minFilter OR job.salaryMin >= minFilter (if max is null)
                // Let's simplify: strict overalp or simple min check?
                // Usually: "Jobs paying at least X" -> job.salaryMax >= X (optimistic) or
                // job.salaryMin >= X (pessimistic)
                // Let's use: job.salaryMax >= minSalary
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("salaryMax"), minSalary));
            }

            if (maxSalary != null) {
                // "Jobs paying at most Y" usually implies fit within budget, less common for
                // candidates.
                // But strictly: job.salaryMin <= maxSalary
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("salaryMin"), maxSalary));
            }

            if (StringUtils.hasText(skill)) {
                // Join skillsRequired
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("skillsRequired")),
                        "%" + skill.toLowerCase() + "%"));
            }

            // Distinct because of joins (skills)
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
