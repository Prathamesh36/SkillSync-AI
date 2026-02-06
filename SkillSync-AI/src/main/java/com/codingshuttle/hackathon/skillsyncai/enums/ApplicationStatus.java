package com.codingshuttle.hackathon.skillsyncai.enums;

/**
 * Status lifecycle for job applications.
 * APPLIED -> SHORTLISTED -> INTERVIEW_SCHEDULED -> HIRED
 * \-> REJECTED
 * Candidate can WITHDRAW at any point before HIRED.
 */
public enum ApplicationStatus {
    APPLIED,
    SHORTLISTED,
    REJECTED,
    INTERVIEW_SCHEDULED,
    HIRED,
    WITHDRAWN
}
