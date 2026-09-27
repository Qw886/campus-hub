package com.campushub.task;

import com.campushub.service.ActivityLifecycleService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ActivityLifecycleTask {
    private final ActivityLifecycleService lifecycleService;

    public ActivityLifecycleTask(ActivityLifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void finishExpiredActivities() {
        lifecycleService.finishExpiredActivities();
    }
}
