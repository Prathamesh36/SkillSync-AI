package com.codingshuttle.hackathon.skillsyncai.service.impl;

import com.codingshuttle.hackathon.skillsyncai.entity.InterviewSchedule;
import com.codingshuttle.hackathon.skillsyncai.entity.JobInvitation;
import com.codingshuttle.hackathon.skillsyncai.service.CalendarInviteService;
import com.codingshuttle.hackathon.skillsyncai.service.NotificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;
    private final CalendarInviteService calendarInviteService;

    @Value("${spring.application.base-url:http://localhost:5173}")
    private String baseUrl;

    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter
            .ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");

    @Override
    @Async("notificationExecutor")
    public void sendJobInvitationEmail(JobInvitation invitation) {
        String to = invitation.getCandidate().getUser().getEmail();
        String subject = "You're invited to apply: " + invitation.getJob().getTitle();
        String acceptLink = baseUrl + "/invitations/accept?token=" + invitation.getInvitationToken();

        String htmlBody = String.format(
                """
                        <html>
                        <body>
                            <h2>Job Invitation</h2>
                            <p>Hello %s,</p>
                            <p>You have been invited to apply for the position of <strong>%s</strong> at <strong>%s</strong>.</p>
                            <p>Recruiter Message:</p>
                            <blockquote>%s</blockquote>
                            <p>Click the link below to accept the invitation and apply:</p>
                            <a href="%s">Apply Now</a>
                            <p><small>This link expires on %s</small></p>
                        </body>
                        </html>
                        """,
                invitation.getCandidate().getUser().getName(),
                invitation.getJob().getTitle(),
                invitation.getJob().getCompanyName(),
                invitation.getMessage() != null ? invitation.getMessage() : "We think you're a great fit!",
                acceptLink,
                invitation.getExpiresAt().toString());

        sendHtmlEmail(to, subject, htmlBody);
    }

    @Override
    public void sendInterviewScheduledNotification(InterviewSchedule interview) {
        String jobTitle = interview.getJobApplication().getJob().getTitle();
        String company = interview.getJobApplication().getJob().getCompanyName();
        String subject = String.format("Interview Scheduled: %s at %s", jobTitle, company);

        String icsContent = calendarInviteService.generateScheduleInvite(interview);

        sendEmailWithCalendarInvite(interview.getCandidate().getUser().getEmail(), subject,
                buildScheduledEmailBody(interview, true), icsContent, "interview-invite.ics");
        sendEmailWithCalendarInvite(interview.getRecruiter().getUser().getEmail(), subject,
                buildScheduledEmailBody(interview, false), icsContent, "interview-invite.ics");
    }

    @Override
    public void sendInterviewRescheduledNotification(InterviewSchedule interview) {
        String jobTitle = interview.getJobApplication().getJob().getTitle();
        String company = interview.getJobApplication().getJob().getCompanyName();
        String subject = String.format("Interview Rescheduled: %s at %s", jobTitle, company);

        String icsContent = calendarInviteService.generateRescheduleInvite(interview);

        sendEmailWithCalendarInvite(interview.getCandidate().getUser().getEmail(), subject,
                buildRescheduledEmailBody(interview, true), icsContent, "interview-updated.ics");
        sendEmailWithCalendarInvite(interview.getRecruiter().getUser().getEmail(), subject,
                buildRescheduledEmailBody(interview, false), icsContent, "interview-updated.ics");
    }

    @Override
    public void sendInterviewCancelledNotification(InterviewSchedule interview, String reason) {
        String jobTitle = interview.getJobApplication().getJob().getTitle();
        String company = interview.getJobApplication().getJob().getCompanyName();
        String subject = String.format("Interview CANCELLED: %s at %s", jobTitle, company);

        String icsContent = calendarInviteService.generateCancelInvite(interview);

        sendEmailWithCalendarInvite(interview.getCandidate().getUser().getEmail(), subject,
                buildCancelledEmailBody(interview, reason, true), icsContent, "interview-cancelled.ics");
        sendEmailWithCalendarInvite(interview.getRecruiter().getUser().getEmail(), subject,
                buildCancelledEmailBody(interview, reason, false), icsContent, "interview-cancelled.ics");
    }

    private void sendHtmlEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email: {}", e.getMessage());
        }
    }

    private void sendEmailWithCalendarInvite(String to, String subject, String body, String icsContent,
            String icsFileName) {
        if (to == null || to.isBlank())
            return;
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.addAttachment(icsFileName, new ByteArrayResource(icsContent.getBytes(StandardCharsets.UTF_8)),
                    "text/calendar");
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send calendar invite: {}", e.getMessage());
        }
    }

    private String buildScheduledEmailBody(InterviewSchedule interview, boolean isCandidate) {
        String jobTitle = interview.getJobApplication().getJob().getTitle();
        String company = interview.getJobApplication().getJob().getCompanyName();
        String dateTime = interview.getInterviewDateTime().format(DISPLAY_DATE_FORMAT);
        String duration = interview.getDurationMinutes() + " minutes";
        String mode = interview.getMode().name();
        String meetingLink = interview.getMeetingLink();

        String greeting = isCandidate ? "Dear " + interview.getCandidate().getUser().getName() + ","
                : "Dear " + interview.getRecruiter().getUser().getName() + ",";
        String intro = isCandidate ? "Your interview has been scheduled!"
                : "You have scheduled an interview with " + interview.getCandidate().getUser().getName() + ".";

        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2>Interview Scheduled</h2>
                    <p>%s</p>
                    <p>%s</p>
                    <div style="background: #f8f9fa; padding: 15px; border-radius: 8px;">
                        <p><strong>Position:</strong> %s</p>
                        <p><strong>Company:</strong> %s</p>
                        <p><strong>Date & Time:</strong> %s</p>
                        <p><strong>Duration:</strong> %s</p>
                        <p><strong>Mode:</strong> %s</p>
                        %s
                    </div>
                </body>
                </html>
                """, greeting, intro, jobTitle, company, dateTime, duration, mode,
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
        String greeting = isCandidate ? "Dear " + interview.getCandidate().getUser().getName() + ","
                : "Dear " + interview.getRecruiter().getUser().getName() + ",";

        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2>Interview Rescheduled</h2>
                    <p>%s</p>
                    <div style="background: #fff3cd; padding: 15px; border-radius: 8px;">
                        <p><strong>Position:</strong> %s</p>
                        <p><strong>Company:</strong> %s</p>
                        <p><del>Previous:</del> %s</p>
                        <p><strong>New Date & Time:</strong> %s</p>
                    </div>
                </body>
                </html>
                """, greeting, jobTitle, company, previousDateTime, newDateTime);
    }

    private String buildCancelledEmailBody(InterviewSchedule interview, String reason, boolean isCandidate) {
        String jobTitle = interview.getJobApplication().getJob().getTitle();
        String company = interview.getJobApplication().getJob().getCompanyName();
        String dateTime = interview.getInterviewDateTime().format(DISPLAY_DATE_FORMAT);
        String greeting = isCandidate ? "Dear " + interview.getCandidate().getUser().getName() + ","
                : "Dear " + interview.getRecruiter().getUser().getName() + ",";

        return String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                    <h2>Interview Cancelled</h2>
                    <p>%s</p>
                    <div style="background: #f8d7da; padding: 15px; border-radius: 8px;">
                        <p><strong>Position:</strong> %s</p>
                        <p><strong>Company:</strong> %s</p>
                        <p><strong>Scheduled:</strong> %s</p>
                        <p><strong>Reason:</strong> %s</p>
                    </div>
                </body>
                </html>
                """, greeting, jobTitle, company, dateTime, reason != null ? reason : "Not specified");
    }
}
