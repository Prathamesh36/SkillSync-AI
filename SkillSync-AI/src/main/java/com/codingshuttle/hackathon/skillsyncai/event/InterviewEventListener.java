package com.codingshuttle.hackathon.skillsyncai.event;

import com.codingshuttle.hackathon.skillsyncai.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Async event listener for interview-related domain events.
 * 
 * Key design decisions:
 * - @Async ensures email sending doesn't block the main thread
 * - Uses dedicated thread pool (notificationExecutor) for isolation
 * - Errors are logged but don't propagate to caller (fire-and-forget)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InterviewEventListener {

    private final NotificationService notificationService;

    /**
     * Handle interview scheduled event.
     * Sends confirmation email with calendar invite to both parties.
     */
    @Async("notificationExecutor")
    @EventListener
    public void handleInterviewScheduled(InterviewScheduledEvent event) {
        log.info("Handling InterviewScheduledEvent for interviewId={}", event.getInterview().getId());
        try {
            notificationService.sendInterviewScheduledNotification(event.getInterview());
        } catch (Exception e) {
            // Log and swallow - email failure shouldn't affect business logic
            log.error("Failed to send scheduled notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle interview rescheduled event.
     * Sends updated calendar invite to both parties.
     */
    @Async("notificationExecutor")
    @EventListener
    public void handleInterviewRescheduled(InterviewRescheduledEvent event) {
        log.info("Handling InterviewRescheduledEvent for interviewId={}", event.getInterview().getId());
        try {
            notificationService.sendInterviewRescheduledNotification(event.getInterview());
        } catch (Exception e) {
            log.error("Failed to send rescheduled notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle interview cancelled event.
     * Sends cancellation calendar invite to both parties.
     */
    @Async("notificationExecutor")
    @EventListener
    public void handleInterviewCancelled(InterviewCancelledEvent event) {
        log.info("Handling InterviewCancelledEvent for interviewId={}", event.getInterview().getId());
        try {
            notificationService.sendInterviewCancelledNotification(
                    event.getInterview(),
                    event.getCancellationReason());
        } catch (Exception e) {
            log.error("Failed to send cancellation notification: {}", e.getMessage(), e);
        }
    }
}
