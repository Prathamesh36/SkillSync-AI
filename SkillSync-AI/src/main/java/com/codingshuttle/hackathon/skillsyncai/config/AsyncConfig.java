package com.codingshuttle.hackathon.skillsyncai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for async processing.
 * Used primarily for non-blocking email/notification sending.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Thread pool for async tasks (email sending, notifications).
     * Configured to handle notification bursts without blocking main threads.
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Minimum threads
        executor.setMaxPoolSize(5); // Max threads for burst handling
        executor.setQueueCapacity(100); // Queue for pending tasks
        executor.setThreadNamePrefix("notification-");
        executor.initialize();
        return executor;
    }
}
