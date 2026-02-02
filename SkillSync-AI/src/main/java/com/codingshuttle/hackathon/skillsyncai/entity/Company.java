package com.codingshuttle.hackathon.skillsyncai.entity;

import com.codingshuttle.hackathon.skillsyncai.enums.HiringProcessType;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;
    private String website;
    private String logoUrl;

    @Column(columnDefinition = "TEXT")
    private String aiCompanySummary; // AI-generated company profile

    // Company details
    private String industry;
    private String size; // 1-10, 11-50, 51-200, 201-500, 501-1000, 1000+
    private String headquarters;
    private Integer foundedYear;

    @ElementCollection
    @CollectionTable(name = "company_locations")
    private List<String> locations = new ArrayList<>();

    @OneToMany(mappedBy = "company")
    private List<Recruiter> recruiters = new ArrayList<>();

    @OneToMany(mappedBy = "company")
    private List<Job> jobs = new ArrayList<>();

    @OneToMany(mappedBy = "company")
    private List<Department> departments = new ArrayList<>();

    // Hiring preferences
    @Enumerated(EnumType.STRING)
    private HiringProcessType hiringProcessType;

    private Integer averageHiringTimelineDays;
    private String applicationPortalUrl;
}
