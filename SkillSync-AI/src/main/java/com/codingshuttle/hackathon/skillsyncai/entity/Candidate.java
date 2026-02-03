package com.codingshuttle.hackathon.skillsyncai.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidates")
public class Candidate extends User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private User user;

    private String fullName;
    private String location;
    private int experienceYears;

    @Column(columnDefinition = "jsonb")
    private String skillsJson;   // ["Java","Spring","Docker"]

    private String headline;
}
