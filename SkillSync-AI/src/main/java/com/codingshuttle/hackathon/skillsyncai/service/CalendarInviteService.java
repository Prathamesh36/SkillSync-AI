package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.entity.InterviewSchedule;

public interface CalendarInviteService {
    String generateScheduleInvite(InterviewSchedule interview);

    String generateRescheduleInvite(InterviewSchedule interview);

    String generateCancelInvite(InterviewSchedule interview);
}
