package com.codingshuttle.hackathon.skillsyncai.service.impl;

import com.codingshuttle.hackathon.skillsyncai.entity.InterviewSchedule;
import com.codingshuttle.hackathon.skillsyncai.enums.RealInterviewMode;
import com.codingshuttle.hackathon.skillsyncai.service.CalendarInviteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class CalendarInviteServiceImpl implements CalendarInviteService {

    private static final DateTimeFormatter ICS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    private static final String PRODID = "-//SkillSync-AI//Interview//EN";

    @Override
    public String generateScheduleInvite(InterviewSchedule interview) {
        log.debug("Generating schedule invite for interviewId={}", interview.getId());
        return buildIcsContent(interview, "REQUEST", "CONFIRMED", 0);
    }

    @Override
    public String generateRescheduleInvite(InterviewSchedule interview) {
        log.debug("Generating reschedule invite for interviewId={}", interview.getId());
        return buildIcsContent(interview, "REQUEST", "CONFIRMED", 1);
    }

    @Override
    public String generateCancelInvite(InterviewSchedule interview) {
        log.debug("Generating cancel invite for interviewId={}", interview.getId());
        return buildIcsContent(interview, "CANCEL", "CANCELLED", 2);
    }

    private String buildIcsContent(InterviewSchedule interview, String method, String status, int sequence) {
        String jobTitle = interview.getJobApplication().getJob().getTitle();
        String candidateName = interview.getCandidate().getUser().getName();
        String candidateEmail = interview.getCandidate().getUser().getEmail();
        String recruiterName = interview.getRecruiter().getUser().getName();
        String recruiterEmail = interview.getRecruiter().getUser().getEmail();

        LocalDateTime startTime = interview.getInterviewDateTime();
        LocalDateTime endTime = startTime.plusMinutes(interview.getDurationMinutes());

        String dtStart = formatToUtc(startTime);
        String dtEnd = formatToUtc(endTime);
        String dtStamp = formatToUtc(LocalDateTime.now());
        String uid = "interview-" + interview.getId() + "@skillsync.ai";

        String description = buildDescription(interview);
        String location = buildLocation(interview);

        StringBuilder ics = new StringBuilder();
        ics.append("BEGIN:VCALENDAR\r\n");
        ics.append("VERSION:2.0\r\n");
        ics.append("PRODID:").append(PRODID).append("\r\n");
        ics.append("METHOD:").append(method).append("\r\n");
        ics.append("BEGIN:VEVENT\r\n");
        ics.append("UID:").append(uid).append("\r\n");
        ics.append("DTSTAMP:").append(dtStamp).append("\r\n");
        ics.append("DTSTART:").append(dtStart).append("\r\n");
        ics.append("DTEND:").append(dtEnd).append("\r\n");
        ics.append("SUMMARY:Interview - ").append(jobTitle).append("\r\n");
        ics.append("DESCRIPTION:").append(description).append("\r\n");
        ics.append("LOCATION:").append(location).append("\r\n");
        ics.append("ORGANIZER;CN=").append(recruiterName).append(":mailto:").append(recruiterEmail).append("\r\n");
        ics.append("ATTENDEE;CN=").append(candidateName).append(";RSVP=TRUE:mailto:").append(candidateEmail)
                .append("\r\n");
        ics.append("STATUS:").append(status).append("\r\n");
        ics.append("SEQUENCE:").append(sequence).append("\r\n");
        ics.append("END:VEVENT\r\n");
        ics.append("END:VCALENDAR\r\n");

        return ics.toString();
    }

    private String buildDescription(InterviewSchedule interview) {
        StringBuilder desc = new StringBuilder();
        desc.append("Interview for ").append(interview.getJobApplication().getJob().getTitle());
        desc.append(" at ").append(interview.getJobApplication().getJob().getCompanyName());
        desc.append("\\n\\nMode: ").append(interview.getMode().name());
        desc.append("\\nDuration: ").append(interview.getDurationMinutes()).append(" minutes");
        if (interview.getMode() == RealInterviewMode.ONLINE && interview.getMeetingLink() != null) {
            desc.append("\\n\\nMeeting Link: ").append(interview.getMeetingLink());
        }
        desc.append("\\n\\nCandidate: ").append(interview.getCandidate().getUser().getName());
        desc.append("\\nRecruiter: ").append(interview.getRecruiter().getUser().getName());
        return desc.toString();
    }

    private String buildLocation(InterviewSchedule interview) {
        if (interview.getMode() == RealInterviewMode.ONLINE) {
            return interview.getMeetingLink() != null ? interview.getMeetingLink() : "Online (Link TBD)";
        } else {
            return interview.getJobApplication().getJob().getCompanyName() + " Office";
        }
    }

    private String formatToUtc(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("UTC"))
                .format(ICS_DATE_FORMAT);
    }
}
