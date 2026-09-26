package com.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campushub.common.exception.BusinessException;
import com.campushub.dto.ActivityQueryRequest;
import com.campushub.entity.Activity;
import com.campushub.mapper.ActivityMapper;
import com.campushub.service.ActivityService;
import com.campushub.vo.ActivityDetailResponse;
import com.campushub.vo.ActivityListResponse;
import com.campushub.vo.ActivityPageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ActivityServiceImpl implements ActivityService {


    @Autowired
    private ActivityMapper activityMapper;


    @Override
    public ActivityPageResponse listActivities(ActivityQueryRequest request) {
        LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Activity::getStatus , 2);
        if (request.getCategoryId() != null) {
            queryWrapper.eq(Activity::getCategoryId, request.getCategoryId());
        }
        String keyword = request.getKeyword();
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like(Activity::getTitle, keyword.trim());
        }
        queryWrapper.orderByDesc(Activity::getId);
        Page<Activity> page =
                new Page<>(request.getPage(), request.getSize());
        Page<Activity> result =
                activityMapper.selectPage(page, queryWrapper);
        List<Activity> activities = result.getRecords();
        List<ActivityListResponse> activityListResponses = new ArrayList<>();
        for (Activity activity : activities) {
            ActivityListResponse response = new ActivityListResponse(activity.getId(),
                    activity.getCategoryId(),
                    activity.getTitle(),
                    activity.getCoverUrl(),
                    activity.getLocation(),
                    activity.getActivityStartTime(),
                    activity.getStatus()
            );
            activityListResponses.add(response);

        }


        return new ActivityPageResponse(
                activityListResponses,
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        );
    }

    /*
    * 活动详情
    * */
    @Override
    public ActivityDetailResponse getPublicDetail(Long id) {

        //检查编号是否合法
        if (id == null || id <= 0){
            throw new BusinessException(400,"活动编号1必须大于0");
        }

        //根据主键查询一场活动
        Activity activity = activityMapper.selectById(id);
        //没查到
        if (activity == null){
            throw new BusinessException(404, "活动不存在");
        }

        //只允许公开查看报名中,已结束的活动
        Integer status = activity.getStatus();
        if (!Integer.valueOf(2).equals(status) && !Integer.valueOf(3).equals(status)){
            throw new BusinessException(404,"活动不存在");
        }
        //5.转换成详情相应
        /*字段：Long id/categoryId；String title/description/coverUrl/location；Integer maxParticipants/currentParticipants/status；
    LocalDateTime signupStartTime/signupEndTime/activityStartTime/activityEndTime。*/
        return new ActivityDetailResponse(
                activity.getId(),
                activity.getCategoryId(),
                activity.getTitle(),
                activity.getDescription(),
                activity.getCoverUrl(),
                activity.getLocation(),
                activity.getMaxParticipants(),
                activity.getCurrentParticipants(),
                activity.getStatus(),
                activity.getSignupStartTime(),
                activity.getSignupEndTime(),
                activity.getActivityStartTime(),
                activity.getActivityEndTime()

        );

    }


}
