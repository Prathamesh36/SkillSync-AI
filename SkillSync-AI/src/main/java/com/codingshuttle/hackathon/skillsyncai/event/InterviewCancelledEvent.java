package com.codingshuttle.hackathon.skillsyncai.event;

import com.codingshuttle.hackathon.skillsyncai.entity.InterviewSchedule;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Domain event published when an interview is cancelled.
 * Triggers cancellation calendar invite notification.
 */
@Getter
public class InterviewCancelledEvent extends ApplicationEvent {

    private final InterviewSchedule interview;
    private final String cancellationReason;

    public InterviewCancelledEvent(Object source, InterviewSchedule interview, String cancellationReason) {
        super(source);
        this.interview = interview;
        this.cancellationReason = cancellationReason;
    }
}
