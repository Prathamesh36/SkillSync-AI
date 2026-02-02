package com.codingshuttle.hackathon.skillsyncai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "educations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Education {
    // Education entity fields and methods would go here
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


}
