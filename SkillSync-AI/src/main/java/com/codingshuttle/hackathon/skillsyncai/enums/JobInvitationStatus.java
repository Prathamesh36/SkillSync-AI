package com.codingshuttle.hackathon.skillsyncai.enums;

/**
 * Status of a job invitation sent to a candidate.
 */
public enum JobInvitationStatus {
    /** Invitation sent, awaiting candidate response */
    SENT,
    /** Candidate accepted the invitation */
    ACCEPTED,
    /** Candidate declined the invitation */
    DECLINED,
    /** Invitation expired before response */
    EXPIRED
}
