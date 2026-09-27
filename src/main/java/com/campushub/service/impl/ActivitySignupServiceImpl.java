package com.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campushub.common.exception.BusinessException;
import com.campushub.dto.SignupQueryRequest;
import com.campushub.entity.Activity;
import com.campushub.entity.ActivityCheckin;
import com.campushub.entity.ActivitySignup;
import com.campushub.mapper.ActivityCheckinMapper;
import com.campushub.mapper.ActivityMapper;
import com.campushub.mapper.ActivitySignupMapper;
import com.campushub.service.ActivitySignupService;
import com.campushub.vo.MySignupPageResponse;
import com.campushub.vo.MySignupResponse;
import com.campushub.vo.SignupStateResponse;
import com.campushub.vo.ActivityDetailResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ActivitySignupServiceImpl implements ActivitySignupService {
    private final ActivityMapper activityMapper;
    private final ActivitySignupMapper signupMapper;
    private final ActivityCheckinMapper checkinMapper;

    public ActivitySignupServiceImpl(ActivityMapper activityMapper,
                                     ActivitySignupMapper signupMapper,
                                     ActivityCheckinMapper checkinMapper) {
        this.activityMapper = activityMapper;
        this.signupMapper = signupMapper;
        this.checkinMapper = checkinMapper;
    }

    @Override
    @Transactional
    public void signup(Long userId, Long activityId) {
        requireIds(userId, activityId);
        Activity activity = activityMapper.selectByIdForUpdate(activityId);
        if (activity == null) throw new BusinessException(404, "活动不存在");

        LocalDateTime now = LocalDateTime.now();
        if (!Integer.valueOf(2).equals(activity.getStatus())
                || activity.getSignupStartTime() == null
                || activity.getSignupEndTime() == null
                || now.isBefore(activity.getSignupStartTime())
                || !now.isBefore(activity.getSignupEndTime())) {
            throw new BusinessException(409, "当前不在活动报名时间内");
        }

        ActivitySignup existing = signupMapper.selectOne(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getUserId, userId)
                .eq(ActivitySignup::getActivityId, activityId));
        if (existing != null && Integer.valueOf(1).equals(existing.getStatus())) {
            throw new BusinessException(409, "你已经报名该活动");
        }

        if (activity.getCurrentParticipants() == null || activity.getMaxParticipants() == null
                || activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            throw new BusinessException(409, "活动报名人数已满");
        }

        if (existing == null) {
            ActivitySignup signup = new ActivitySignup();
            signup.setUserId(userId);
            signup.setActivityId(activityId);
            signup.setStatus(1);
            signup.setSignupTime(now);
            if (signupMapper.insert(signup) != 1) {
                throw new BusinessException(409, "报名失败");
            }
        } else {
            int rows = signupMapper.update(null, new LambdaUpdateWrapper<ActivitySignup>()
                    .eq(ActivitySignup::getId, existing.getId())
                    .set(ActivitySignup::getStatus, 1)
                    .set(ActivitySignup::getSignupTime, now)
                    .set(ActivitySignup::getCancelTime, null));
            if (rows != 1) throw new BusinessException(409, "报名失败");
        }
        if (activityMapper.incrementParticipants(activityId) != 1) {
            throw new BusinessException(409, "活动报名人数已满");
        }
    }

    @Override
    @Transactional
    public void cancel(Long userId, Long activityId) {
        requireIds(userId, activityId);
        Activity activity = activityMapper.selectByIdForUpdate(activityId);
        if (activity == null) throw new BusinessException(404, "活动不存在");
        ActivitySignup signup = signupMapper.selectOne(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getUserId, userId)
                .eq(ActivitySignup::getActivityId, activityId));
        if (signup == null) {
            throw new BusinessException(404, "没有找到有效报名记录");
        }
        if (Integer.valueOf(2).equals(signup.getStatus())) return;
        if (!Integer.valueOf(1).equals(signup.getStatus())) {
            throw new BusinessException(409, "报名记录状态异常");
        }
        LocalDateTime now = LocalDateTime.now();
        if (!Integer.valueOf(2).equals(activity.getStatus())
                || activity.getSignupEndTime() == null
                || !now.isBefore(activity.getSignupEndTime())) {
            throw new BusinessException(409, "当前不能取消报名");
        }
        if (checkinMapper.selectCount(new LambdaQueryWrapper<ActivityCheckin>()
                .eq(ActivityCheckin::getActivityId, activityId)
                .eq(ActivityCheckin::getUserId, userId)) > 0) {
            throw new BusinessException(409, "已签到不能取消报名");
        }
        int rows = signupMapper.update(null, new LambdaUpdateWrapper<ActivitySignup>()
                .eq(ActivitySignup::getId, signup.getId())
                .eq(ActivitySignup::getStatus, 1)
                .set(ActivitySignup::getStatus, 2)
                .set(ActivitySignup::getCancelTime, now));
        if (rows != 1) throw new BusinessException(409, "取消报名失败");
        if (activityMapper.decrementParticipants(activityId) != 1) {
            throw new BusinessException(409, "取消报名失败");
        }
    }

    @Override
    public MySignupPageResponse listMine(Long userId, SignupQueryRequest request) {
        if (userId == null) throw new BusinessException(401, "请先登录");
        LambdaQueryWrapper<ActivitySignup> wrapper = new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getUserId, userId);
        if (request.getStatus() != null) wrapper.eq(ActivitySignup::getStatus, request.getStatus());
        wrapper.orderByDesc(ActivitySignup::getSignupTime).orderByDesc(ActivitySignup::getId);
        Page<ActivitySignup> page = signupMapper.selectPage(new Page<>(request.getPage(), request.getSize()), wrapper);
        List<ActivitySignup> signups = page.getRecords();
        if (signups.isEmpty()) return new MySignupPageResponse(List.of(), page.getTotal(), page.getCurrent(), page.getSize());

        List<Long> ids = signups.stream().map(ActivitySignup::getActivityId).toList();
        Map<Long, Activity> activities = new HashMap<>();
        activityMapper.selectBatchIds(ids).forEach(a -> activities.put(a.getId(), a));
        Set<Long> checkedActivityIds = new HashSet<>();
        checkinMapper.selectList(new LambdaQueryWrapper<ActivityCheckin>()
                .eq(ActivityCheckin::getUserId, userId)
                .in(ActivityCheckin::getActivityId, ids))
                .forEach(c -> checkedActivityIds.add(c.getActivityId()));

        List<MySignupResponse> records = new ArrayList<>();
        for (ActivitySignup signup : signups) {
            Activity activity = activities.get(signup.getActivityId());
            if (activity == null) continue;
            records.add(new MySignupResponse(signup.getId(), activity.getId(), activity.getTitle(),
                    activity.getLocation(), activity.getStatus(), signup.getStatus(), signup.getSignupTime(),
                    signup.getCancelTime(), activity.getActivityStartTime(), checkedActivityIds.contains(activity.getId())));
        }
        return new MySignupPageResponse(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public SignupStateResponse getState(Long userId, Long activityId) {
        requireIds(userId, activityId);
        ActivitySignup signup = signupMapper.selectOne(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getUserId, userId)
                .eq(ActivitySignup::getActivityId, activityId));
        boolean active = signup != null && Integer.valueOf(1).equals(signup.getStatus());
        if (!active) return new SignupStateResponse(false, false);
        boolean checkedIn = checkinMapper.selectCount(new LambdaQueryWrapper<ActivityCheckin>()
                .eq(ActivityCheckin::getUserId, userId)
                .eq(ActivityCheckin::getActivityId, activityId)) > 0;
        return new SignupStateResponse(true, checkedIn);
    }

    @Override
    public ActivityDetailResponse getMineDetail(Long userId, Long activityId) {
        requireIds(userId, activityId);
        Long count = signupMapper.selectCount(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getUserId, userId)
                .eq(ActivitySignup::getActivityId, activityId));
        if (count == 0) throw new BusinessException(404, "没有找到你的报名记录");
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) throw new BusinessException(404, "活动不存在");
        return new ActivityDetailResponse(activity.getId(), activity.getCategoryId(), activity.getTitle(),
                activity.getDescription(), activity.getCoverUrl(), activity.getLocation(),
                activity.getMaxParticipants(), activity.getCurrentParticipants(), activity.getStatus(),
                activity.getSignupStartTime(), activity.getSignupEndTime(),
                activity.getActivityStartTime(), activity.getActivityEndTime());
    }

    private void requireIds(Long userId, Long activityId) {
        if (userId == null) throw new BusinessException(401, "请先登录");
        if (activityId == null || activityId <= 0) throw new BusinessException(400, "活动编号必须大于0");
    }
}
