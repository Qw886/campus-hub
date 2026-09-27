package com.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campushub.common.exception.BusinessException;
import com.campushub.entity.Activity;
import com.campushub.entity.ActivityCheckin;
import com.campushub.entity.ActivitySignup;
import com.campushub.mapper.ActivityCheckinMapper;
import com.campushub.mapper.ActivityMapper;
import com.campushub.mapper.ActivitySignupMapper;
import com.campushub.service.ActivityCheckinService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ActivityCheckinServiceImpl implements ActivityCheckinService {
    private final ActivityMapper activityMapper;
    private final ActivitySignupMapper signupMapper;
    private final ActivityCheckinMapper checkinMapper;

    public ActivityCheckinServiceImpl(ActivityMapper activityMapper,
                                      ActivitySignupMapper signupMapper,
                                      ActivityCheckinMapper checkinMapper) {
        this.activityMapper = activityMapper;
        this.signupMapper = signupMapper;
        this.checkinMapper = checkinMapper;
    }

    @Override
    @Transactional
    public void checkin(Long userId, Long activityId) {
        if (userId == null) throw new BusinessException(401, "请先登录");
        if (activityId == null || activityId <= 0) throw new BusinessException(400, "活动编号必须大于0");
        Activity activity = activityMapper.selectByIdForUpdate(activityId);
        if (activity == null) throw new BusinessException(404, "活动不存在");
        LocalDateTime now = LocalDateTime.now();
        if (!Integer.valueOf(2).equals(activity.getStatus())
                || activity.getActivityStartTime() == null || activity.getActivityEndTime() == null
                || now.isBefore(activity.getActivityStartTime().minusMinutes(30))
                || !now.isBefore(activity.getActivityEndTime())) {
            throw new BusinessException(409, "当前不在签到时间内");
        }
        ActivitySignup signup = signupMapper.selectOne(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getUserId, userId)
                .eq(ActivitySignup::getActivityId, activityId)
                .eq(ActivitySignup::getStatus, 1));
        if (signup == null) throw new BusinessException(409, "只有有效报名用户才能签到");
        if (checkinMapper.selectCount(new LambdaQueryWrapper<ActivityCheckin>()
                .eq(ActivityCheckin::getActivityId, activityId)
                .eq(ActivityCheckin::getUserId, userId)) > 0) {
            throw new BusinessException(409, "你已经签到过了");
        }
        ActivityCheckin checkin = new ActivityCheckin();
        checkin.setActivityId(activityId);
        checkin.setUserId(userId);
        checkin.setSignupId(signup.getId());
        checkin.setCheckinTime(now);
        if (checkinMapper.insert(checkin) != 1) throw new BusinessException(409, "签到失败");
    }
}
