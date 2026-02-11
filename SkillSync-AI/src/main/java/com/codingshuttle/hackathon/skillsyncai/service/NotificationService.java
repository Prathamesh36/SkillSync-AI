package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.entity.InterviewSchedule;
import com.codingshuttle.hackathon.skillsyncai.entity.JobInvitation;

public interface NotificationService {
    void sendJobInvitationEmail(JobInvitation invitation);

    void sendInterviewScheduledNotification(InterviewSchedule interview);

    void sendInterviewRescheduledNotification(InterviewSchedule interview);

    void sendInterviewCancelledNotification(InterviewSchedule interview, String reason);
}
