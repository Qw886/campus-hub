package com.campushub.service.impl;

import com.campushub.mapper.ActivityMapper;
import com.campushub.service.ActivityLifecycleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ActivityLifecycleServiceImpl implements ActivityLifecycleService {
    private final ActivityMapper activityMapper;

    public ActivityLifecycleServiceImpl(ActivityMapper activityMapper) {
        this.activityMapper = activityMapper;
    }

    @Override
    @Transactional
    public int finishExpiredActivities() {
        return activityMapper.finishExpired(LocalDateTime.now());
    }
}
