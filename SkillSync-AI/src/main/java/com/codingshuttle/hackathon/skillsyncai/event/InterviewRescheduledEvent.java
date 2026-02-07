package com.codingshuttle.hackathon.skillsyncai.event;

import com.codingshuttle.hackathon.skillsyncai.entity.InterviewSchedule;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Domain event published when an interview is rescheduled.
 * Triggers updated calendar invite notification.
 */
@Getter
public class InterviewRescheduledEvent extends ApplicationEvent {

    private final InterviewSchedule interview;
    private final LocalDateTime previousDateTime;

    public InterviewRescheduledEvent(Object source, InterviewSchedule interview, LocalDateTime previousDateTime) {
        super(source);
        this.interview = interview;
        this.previousDateTime = previousDateTime;
    }
}
