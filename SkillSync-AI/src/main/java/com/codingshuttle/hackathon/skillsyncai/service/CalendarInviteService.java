package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.entity.InterviewSchedule;
import com.codingshuttle.hackathon.skillsyncai.enums.RealInterviewMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Service for generating iCalendar (.ics) files.
 * Implements RFC 5545 for calendar interoperability.
 * 
 * Supports:
 * - Google Calendar
 * - Microsoft Outlook
 * - Apple Calendar
 */
@Service
@Slf4j
public class CalendarInviteService {

    // iCalendar date format: 20260207T150000Z
    private static final DateTimeFormatter ICS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private static final String PRODID = "-//SkillSync-AI//Interview//EN";

    /**
     * Generate .ics content for a newly scheduled interview.
     * METHOD:REQUEST creates a new event in the recipient's calendar.
     */
    public String generateScheduleInvite(InterviewSchedule interview) {
        log.debug("Generating schedule invite for interviewId={}", interview.getId());
        return buildIcsContent(interview, "REQUEST", "CONFIRMED", 0);
    }

    /**
     * Generate .ics content for a rescheduled interview.
     * Uses same UID but increments SEQUENCE to update existing event.
     */
    public String generateRescheduleInvite(InterviewSchedule interview) {
        log.debug("Generating reschedule invite for interviewId={}", interview.getId());
        // SEQUENCE > 0 indicates this is an update to an existing event
        return buildIcsContent(interview, "REQUEST", "CONFIRMED", 1);
    }

    /**
     * Generate .ics content for a cancelled interview.
     * METHOD:CANCEL removes the event from the recipient's calendar.
     */
    public String generateCancelInvite(InterviewSchedule interview) {
        log.debug("Generating cancel invite for interviewId={}", interview.getId());
        return buildIcsContent(interview, "CANCEL", "CANCELLED", 2);
    }

    /**
     * Build iCalendar content following RFC 5545.
     * 
     * @param interview The interview schedule entity
     * @param method    REQUEST (create/update) or CANCEL
     * @param status    CONFIRMED, TENTATIVE, or CANCELLED
     * @param sequence  Version number (0 for new, increments for updates)
     */
    private String buildIcsContent(InterviewSchedule interview, String method,
            String status, int sequence) {
        String jobTitle = interview.getJobApplication().getJob().getTitle();
        String candidateName = interview.getCandidate().getUser().getName();
        String candidateEmail = interview.getCandidate().getUser().getEmail();
        String recruiterName = interview.getRecruiter().getUser().getName();
        String recruiterEmail = interview.getRecruiter().getUser().getEmail();

        LocalDateTime startTime = interview.getInterviewDateTime();
        LocalDateTime endTime = startTime.plusMinutes(interview.getDurationMinutes());

        // Convert to UTC for iCalendar format
        String dtStart = formatToUtc(startTime);
        String dtEnd = formatToUtc(endTime);
        String dtStamp = formatToUtc(LocalDateTime.now());

        // Unique ID must remain the same across schedule/reschedule/cancel
        String uid = "interview-" + interview.getId() + "@skillsync.ai";

        // Build description based on mode
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

    /**
     * Build description text for the calendar event.
     */
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

    /**
     * Build location based on interview mode.
     */
    private String buildLocation(InterviewSchedule interview) {
        if (interview.getMode() == RealInterviewMode.ONLINE) {
            return interview.getMeetingLink() != null ? interview.getMeetingLink() : "Online (Link TBD)";
        } else {
            // For offline interviews, use company name as placeholder
            return interview.getJobApplication().getJob().getCompanyName() + " Office";
        }
    }

    /**
     * Format LocalDateTime to UTC iCalendar format.
     */
    private String formatToUtc(LocalDateTime dateTime) {
        // Convert system default timezone to UTC
        return dateTime.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("UTC"))
                .format(ICS_DATE_FORMAT);
    }
}
