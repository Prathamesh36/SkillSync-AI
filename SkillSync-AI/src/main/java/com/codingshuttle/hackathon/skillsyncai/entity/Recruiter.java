package com.codingshuttle.hackathon.skillsyncai.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "candidates")
public class Recruiter extends User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private User user;

    private String companyName;
    private String designation;
}
