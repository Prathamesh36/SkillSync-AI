package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.entity.InterviewSchedule;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * Service for sending interview notification emails.
 * Emails include iCalendar (.ics) attachments for calendar integration.
 * 
 * This service should be called asynchronously to avoid blocking
 * the main business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;
    private final CalendarInviteService calendarInviteService;

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter
            .ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");

    /**
     * Send notification for a newly scheduled interview.
     */
    public void sendInterviewScheduledNotification(InterviewSchedule interview) {
        log.info("Sending scheduled notification for interviewId={}", interview.getId());

        String jobTitle = interview.getJobApplication().getJob().getTitle();
        String company = interview.getJobApplication().getJob().getCompanyName();
        String dateTime = interview.getInterviewDateTime().format(DISPLAY_DATE_FORMAT);

        String subject = String.format("Interview Scheduled: %s at %s", jobTitle, company);

        String candidateBody = buildScheduledEmailBody(interview, true);
        String recruiterBody = buildScheduledEmailBody(interview, false);

        String icsContent = calendarInviteService.generateScheduleInvite(interview);

        // Send to candidate
        sendEmailWithCalendarInvite(
                interview.getCandidate().getUser().getEmail(),
                subject,
                candidateBody,
                icsContent,
                "interview-invite.ics");

        // Send to recruiter
        sendEmailWithCalendarInvite(
                interview.getRecruiter().getUser().getEmail(),
                subject,
                recruiterBody,
                icsContent,
                "interview-invite.ics");
    }

    /**
     * Send notification for a rescheduled interview.
     */
    public void sendInterviewRescheduledNotification(InterviewSchedule interview) {
        log.info("Sending rescheduled notification for interviewId={}", interview.getId());

        String jobTitle = interview.getJobApplication().getJob().getTitle();
        String company = interview.getJobApplication().getJob().getCompanyName();

        String subject = String.format("Interview Rescheduled: %s at %s", jobTitle, company);

        String candidateBody = buildRescheduledEmailBody(interview, true);
        String recruiterBody = buildRescheduledEmailBody(interview, false);

        String icsContent = calendarInviteService.generateRescheduleInvite(interview);

        // Send to candidate
        sendEmailWithCalendarInvite(
                interview.getCandidate().getUser().getEmail(),
                subject,
                candidateBody,
                icsContent,
                "interview-updated.ics");

        // Send to recruiter
        sendEmailWithCalendarInvite(
                interview.getRecruiter().getUser().getEmail(),
                subject,
                recruiterBody,
                icsContent,
                "interview-updated.ics");
    }

    /**
     * Send notification for a cancelled interview.
     */
    public void sendInterviewCancelledNotification(InterviewSchedule interview, String reason) {
        log.info("Sending cancellation notification for interviewId={}", interview.getId());

        String jobTitle = interview.getJobApplication().getJob().getTitle();
        String company = interview.getJobApplication().getJob().getCompanyName();

        String subject = String.format("Interview CANCELLED: %s at %s", jobTitle, company);

        String candidateBody = buildCancelledEmailBody(interview, reason, true);
        String recruiterBody = buildCancelledEmailBody(interview, reason, false);

        String icsContent = calendarInviteService.generateCancelInvite(interview);

        // Send to candidate
        sendEmailWithCalendarInvite(
                interview.getCandidate().getUser().getEmail(),
                subject,
                candidateBody,
                icsContent,
                "interview-cancelled.ics");

        // Send to recruiter
        sendEmailWithCalendarInvite(
                interview.getRecruiter().getUser().getEmail(),
                subject,
                recruiterBody,
                icsContent,
                "interview-cancelled.ics");
    }

    /**
     * Send email with iCalendar attachment.
     */
    private void sendEmailWithCalendarInvite(String to, String subject, String body,
            String icsContent, String icsFileName) {
        if (to == null || to.isBlank()) {
            log.warn("Cannot send email: recipient address is empty");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML

            // Attach .ics file
            byte[] icsBytes = icsContent.getBytes(StandardCharsets.UTF_8);
            helper.addAttachment(icsFileName, new ByteArrayResource(icsBytes), "text/calendar");

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);

        } catch (MessagingException e) {
            // Log error but don't throw - email failure shouldn't break business logic
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }

    // ======================== EMAIL BODY BUILDERS ========================

    private String buildScheduledEmailBody(InterviewSchedule interview, boolean isCandidate) {
        String jobTitle = interview.getJobApplication().getJob().getTitle();
        String company = interview.getJobApplication().getJob().getCompanyName();
        String dateTime = interview.getInterviewDateTime().format(DISPLAY_DATE_FORMAT);
        String duration = interview.getDurationMinutes() + " minutes";
        String mode = interview.getMode().name();
        String meetingLink = interview.getMeetingLink();

        String greeting = isCandidate
                ? "Dear " + interview.getCandidate().getUser().getName() + ","
                : "Dear " + interview.getRecruiter().getUser().getName() + ",";

        String intro = isCandidate
                ? "Your interview has been scheduled!"
                : "You have scheduled an interview with " + interview.getCandidate().getUser().getName() + ".";

        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2 style="color: #2c3e50;">Interview Scheduled</h2>
                    <p>%s</p>
                    <p>%s</p>

                    <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #495057;">Interview Details</h3>
                        <p><strong>Position:</strong> %s</p>
                        <p><strong>Company:</strong> %s</p>
                        <p><strong>Date & Time:</strong> %s</p>
                        <p><strong>Duration:</strong> %s</p>
                        <p><strong>Mode:</strong> %s</p>
                        %s
                    </div>

                    <p>A calendar invite is attached to this email.</p>

                    <p style="color: #6c757d; font-size: 12px;">
                        This is an automated message from SkillSync-AI.
                    </p>
                </body>
                </html>
                """,
                greeting, intro, jobTitle, company, dateTime, duration, mode,
                meetingLink != null
                        ? "<p><strong>Meeting Link:</strong> <a href=\"" + meetingLink + "\">" + meetingLink
                                + "</a></p>"
                        : "");
    }

    private String buildRescheduledEmailBody(InterviewSchedule interview, boolean isCandidate) {
        String jobTitle = interview.getJobApplication().getJob().getTitle();
        String company = interview.getJobApplication().getJob().getCompanyName();
        String newDateTime = interview.getInterviewDateTime().format(DISPLAY_DATE_FORMAT);
        String previousDateTime = interview.getPreviousInterviewDateTime() != null
                ? interview.getPreviousInterviewDateTime().format(DISPLAY_DATE_FORMAT)
                : "N/A";

        String greeting = isCandidate
                ? "Dear " + interview.getCandidate().getUser().getName() + ","
                : "Dear " + interview.getRecruiter().getUser().getName() + ",";

        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2 style="color: #e67e22;">Interview Rescheduled</h2>
                    <p>%s</p>
                    <p>Your interview has been rescheduled to a new date and time.</p>

                    <div style="background: #fff3cd; padding: 15px; border-radius: 8px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #856404;">Updated Details</h3>
                        <p><strong>Position:</strong> %s</p>
                        <p><strong>Company:</strong> %s</p>
                        <p><del style="color: #dc3545;">Previous:</del> %s</p>
                        <p><strong style="color: #28a745;">New Date & Time:</strong> %s</p>
                    </div>

                    <p>Please update your calendar with the attached invite.</p>

                    <p style="color: #6c757d; font-size: 12px;">
                        This is an automated message from SkillSync-AI.
                    </p>
                </body>
                </html>
                """,
                greeting, jobTitle, company, previousDateTime, newDateTime);
    }

    private String buildCancelledEmailBody(InterviewSchedule interview, String reason, boolean isCandidate) {
        String jobTitle = interview.getJobApplication().getJob().getTitle();
        String company = interview.getJobApplication().getJob().getCompanyName();
        String dateTime = interview.getInterviewDateTime().format(DISPLAY_DATE_FORMAT);

        String greeting = isCandidate
                ? "Dear " + interview.getCandidate().getUser().getName() + ","
                : "Dear " + interview.getRecruiter().getUser().getName() + ",";

        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2 style="color: #dc3545;">Interview Cancelled</h2>
                    <p>%s</p>
                    <p>Unfortunately, this interview has been cancelled.</p>

                    <div style="background: #f8d7da; padding: 15px; border-radius: 8px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #721c24;">Cancelled Interview</h3>
                        <p><strong>Position:</strong> %s</p>
                        <p><strong>Company:</strong> %s</p>
                        <p><strong>Originally Scheduled:</strong> %s</p>
                        <p><strong>Reason:</strong> %s</p>
                    </div>

                    <p>The attached calendar invite will remove this event from your calendar.</p>

                    <p style="color: #6c757d; font-size: 12px;">
                        This is an automated message from SkillSync-AI.
                    </p>
                </body>
                </html>
                """,
                greeting, jobTitle, company, dateTime, reason != null ? reason : "Not specified");
    }
}
