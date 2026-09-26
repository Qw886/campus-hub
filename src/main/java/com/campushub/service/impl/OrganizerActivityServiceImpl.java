package com.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campushub.common.exception.BusinessException;
import com.campushub.dto.ActivityCreateRequest;
import com.campushub.dto.OrganizerActivityQueryRequest;
import com.campushub.dto.SignupQueryRequest;
import com.campushub.entity.Activity;
import com.campushub.entity.ActivityCategory;
import com.campushub.entity.ActivityAudit;
import com.campushub.entity.ActivityCheckin;
import com.campushub.entity.ActivitySignup;
import com.campushub.mapper.ActivityCategoryMapper;
import com.campushub.mapper.ActivityAuditMapper;
import com.campushub.mapper.ActivityCheckinMapper;
import com.campushub.mapper.ActivityMapper;
import com.campushub.mapper.ActivitySignupMapper;
import com.campushub.mapper.SysUserMapper;
import com.campushub.service.OrganizerActivityService;
import com.campushub.vo.ActivityDetailResponse;
import com.campushub.vo.OrganizerActivityPageResponse;
import com.campushub.vo.OrganizerActivityResponse;
import com.campushub.vo.ParticipantPageResponse;
import com.campushub.vo.ParticipantResponse;
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
public class OrganizerActivityServiceImpl implements OrganizerActivityService {

    private final ActivityMapper activityMapper;
    private final ActivityCategoryMapper activityCategoryMapper;
    private final ActivitySignupMapper activitySignupMapper;
    private final ActivityCheckinMapper activityCheckinMapper;
    private final ActivityAuditMapper activityAuditMapper;
    private final SysUserMapper sysUserMapper;

    public OrganizerActivityServiceImpl(ActivityMapper activityMapper,
                                        ActivityCategoryMapper activityCategoryMapper,
                                        ActivitySignupMapper activitySignupMapper,
                                        ActivityCheckinMapper activityCheckinMapper,
                                        ActivityAuditMapper activityAuditMapper,
                                        SysUserMapper sysUserMapper) {
        this.activityMapper = activityMapper;
        this.activityCategoryMapper = activityCategoryMapper;
        this.activitySignupMapper = activitySignupMapper;
        this.activityCheckinMapper = activityCheckinMapper;
        this.activityAuditMapper = activityAuditMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    @Transactional
    public Long create(Long organizerId, ActivityCreateRequest request) {

        if (organizerId == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (request == null || request.getCategoryId() == null || request.getMaxParticipants() == null
                || request.getRegistrationStartTime() == null || request.getRegistrationEndTime() == null
                || request.getStartTime() == null || request.getEndTime() == null) {
            throw new BusinessException(400, "活动参数不完整");
        }

        LambdaQueryWrapper<ActivityCategory> query = new LambdaQueryWrapper<>();
        query.eq(ActivityCategory::getId , request.getCategoryId()).eq(ActivityCategory::getStatus , 1);
        ActivityCategory category = activityCategoryMapper.selectOne(query);
        if (category == null) {
            throw new BusinessException(400 , "分类不存在或已停用");
        }

        // 2. 检查四个时间
        LocalDateTime registrationStartTime = request.getRegistrationStartTime();
        LocalDateTime registrationEndTime = request.getRegistrationEndTime();
        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = request.getEndTime();

        // 报名开始 < 报名截止
        if (!registrationStartTime.isBefore(registrationEndTime)) {
            throw new BusinessException(400, "报名开始时间必须早于报名截止时间");
        }

        // 报名截止 <= 活动开始
        if (registrationEndTime.isAfter(startTime)) {
            throw new BusinessException(400, "报名截止时间不能晚于活动开始时间");
        }

        // 活动开始 < 活动结束
        if (!startTime.isBefore(endTime)) {
            throw new BusinessException(400, "活动开始时间必须早于活动结束时间");
        }

        // 报名截止必须在未来
        if (!registrationEndTime.isAfter(LocalDateTime.now())) {
            throw new BusinessException(400, "报名截止时间必须在未来");
        }

        // 3. 创建 Activity，赋入允许提交的字段
        Activity activity = new Activity();

        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setCoverUrl(request.getCoverUrl());
        activity.setLocation(request.getLocation());
        activity.setCategoryId(request.getCategoryId());
        activity.setMaxParticipants(request.getMaxParticipants());

        activity.setSignupStartTime(
                request.getRegistrationStartTime());

        activity.setSignupEndTime(
                request.getRegistrationEndTime());

        activity.setActivityStartTime(request.getStartTime());
        activity.setActivityEndTime(request.getEndTime());

        // 4. 后端自己设置
        activity.setOrganizerId(organizerId);
        activity.setStatus(1);
        activity.setCurrentParticipants(0);

        // 5. 插入数据库
        int rows = activityMapper.insert(activity);

        if (rows != 1) {
            throw new BusinessException(400, "创建活动失败");
        }

        // insert 成功后，MyBatis-Plus 会把生成的 id 回填进 activity
        return activity.getId();
    }

    @Override
    @Transactional
    public void update(Long organizerId, Long activityId, ActivityCreateRequest request) {
        requireOrganizer(organizerId);
        Activity activity = ownedActivityForUpdate(organizerId, activityId);
        if (!Integer.valueOf(1).equals(activity.getStatus()) && !Integer.valueOf(5).equals(activity.getStatus())) {
            throw new BusinessException(409, "只有待审核或审核拒绝的活动可以修改");
        }
        validateRequest(request);
        applyRequest(activity, request);
        // 被拒绝的活动修改后重新进入审核流程。
        activity.setStatus(1);
        if (activityMapper.updateById(activity) != 1) {
            throw new BusinessException(409, "活动已被修改，请刷新后重试");
        }
    }

    @Override
    public OrganizerActivityPageResponse listMine(Long organizerId, OrganizerActivityQueryRequest request) {
        requireOrganizer(organizerId);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<Activity>()
                .eq(Activity::getOrganizerId, organizerId);
        if (request.getStatus() != null) wrapper.eq(Activity::getStatus, request.getStatus());
        wrapper.orderByDesc(Activity::getId);
        Page<Activity> page = activityMapper.selectPage(new Page<>(request.getPage(), request.getSize()), wrapper);
        Map<Long, String> auditReasons = new HashMap<>();
        List<Long> activityIds = page.getRecords().stream().map(Activity::getId).toList();
        if (!activityIds.isEmpty()) {
            activityAuditMapper.selectList(new LambdaQueryWrapper<ActivityAudit>()
                            .in(ActivityAudit::getActivityId, activityIds)
                            .orderByDesc(ActivityAudit::getCreatedAt)
                            .orderByDesc(ActivityAudit::getId))
                    .forEach(audit -> auditReasons.putIfAbsent(audit.getActivityId(), audit.getReason()));
        }
        List<OrganizerActivityResponse> records = page.getRecords().stream()
                .map(a -> new OrganizerActivityResponse(a.getId(), a.getTitle(), a.getStatus(),
                        a.getCurrentParticipants(), a.getMaxParticipants(), a.getActivityStartTime(),
                        auditReasons.get(a.getId())))
                .toList();
        return new OrganizerActivityPageResponse(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public ActivityDetailResponse getMine(Long organizerId, Long activityId) {
        requireOrganizer(organizerId);
        if (activityId == null || activityId <= 0) throw new BusinessException(400, "活动编号必须大于0");
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || !organizerId.equals(activity.getOrganizerId())) {
            throw new BusinessException(404, "活动不存在");
        }
        return detail(activity);
    }

    @Override
    public ParticipantPageResponse listParticipants(Long organizerId, Long activityId, SignupQueryRequest request) {
        requireOrganizer(organizerId);
        Activity activity = ownedActivity(organizerId, activityId);
        LambdaQueryWrapper<ActivitySignup> wrapper = new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, activity.getId());
        if (request.getStatus() != null) wrapper.eq(ActivitySignup::getStatus, request.getStatus());
        wrapper.orderByDesc(ActivitySignup::getSignupTime).orderByDesc(ActivitySignup::getId);
        Page<ActivitySignup> page = activitySignupMapper.selectPage(new Page<>(request.getPage(), request.getSize()), wrapper);
        List<ActivitySignup> signups = page.getRecords();
        if (signups.isEmpty()) return new ParticipantPageResponse(List.of(), page.getTotal(), page.getCurrent(), page.getSize());

        List<Long> userIds = signups.stream().map(ActivitySignup::getUserId).toList();
        Map<Long, com.campushub.entity.SysUser> users = new HashMap<>();
        sysUserMapper.selectBatchIds(userIds).forEach(user -> users.put(user.getId(), user));
        Set<Long> checkedSignupIds = new HashSet<>();
        activityCheckinMapper.selectList(new LambdaQueryWrapper<ActivityCheckin>()
                .eq(ActivityCheckin::getActivityId, activity.getId())
                .in(ActivityCheckin::getSignupId, signups.stream().map(ActivitySignup::getId).toList()))
                .forEach(c -> checkedSignupIds.add(c.getSignupId()));

        List<ParticipantResponse> records = new ArrayList<>();
        for (ActivitySignup signup : signups) {
            com.campushub.entity.SysUser user = users.get(signup.getUserId());
            records.add(new ParticipantResponse(signup.getId(), signup.getUserId(),
                    user == null ? null : user.getUsername(), user == null ? null : user.getNickname(),
                    signup.getStatus(), signup.getSignupTime(), checkedSignupIds.contains(signup.getId())));
        }
        return new ParticipantPageResponse(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional
    public void cancel(Long organizerId, Long activityId) {
        requireOrganizer(organizerId);
        Activity activity = ownedActivityForUpdate(organizerId, activityId);
        if (!Integer.valueOf(1).equals(activity.getStatus()) && !Integer.valueOf(2).equals(activity.getStatus())) {
            throw new BusinessException(409, "当前状态不能取消活动");
        }
        if (activity.getActivityStartTime() != null && !LocalDateTime.now().isBefore(activity.getActivityStartTime())) {
            throw new BusinessException(409, "活动开始后不能取消");
        }
        int rows = activityMapper.update(null, new LambdaUpdateWrapper<Activity>()
                .eq(Activity::getId, activityId)
                .eq(Activity::getOrganizerId, organizerId)
                .in(Activity::getStatus, 1, 2)
                .set(Activity::getStatus, 4));
        if (rows != 1) throw new BusinessException(409, "活动已被修改，请刷新后重试");
    }

    private Activity ownedActivity(Long organizerId, Long activityId) {
        if (activityId == null || activityId <= 0) throw new BusinessException(400, "活动编号必须大于0");
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || !organizerId.equals(activity.getOrganizerId())) throw new BusinessException(404, "活动不存在");
        return activity;
    }

    private Activity ownedActivityForUpdate(Long organizerId, Long activityId) {
        if (activityId == null || activityId <= 0) throw new BusinessException(400, "活动编号必须大于0");
        Activity activity = activityMapper.selectByIdForUpdate(activityId);
        if (activity == null || !organizerId.equals(activity.getOrganizerId())) throw new BusinessException(404, "活动不存在");
        return activity;
    }

    private void requireOrganizer(Long organizerId) {
        if (organizerId == null) throw new BusinessException(401, "请先登录");
    }

    private void validateRequest(ActivityCreateRequest request) {
        if (request == null || request.getCategoryId() == null || request.getMaxParticipants() == null
                || request.getRegistrationStartTime() == null || request.getRegistrationEndTime() == null
                || request.getStartTime() == null || request.getEndTime() == null) {
            throw new BusinessException(400, "活动参数不完整");
        }
        ActivityCategory category = activityCategoryMapper.selectOne(new LambdaQueryWrapper<ActivityCategory>()
                .eq(ActivityCategory::getId, request.getCategoryId()).eq(ActivityCategory::getStatus, 1));
        if (category == null) throw new BusinessException(400, "分类不存在或已停用");
        if (!request.getRegistrationStartTime().isBefore(request.getRegistrationEndTime())) {
            throw new BusinessException(400, "报名开始时间必须早于报名截止时间");
        }
        if (request.getRegistrationEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException(400, "报名截止时间不能晚于活动开始时间");
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BusinessException(400, "活动开始时间必须早于活动结束时间");
        }
        if (!request.getRegistrationEndTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException(400, "报名截止时间必须在未来");
        }
    }

    private void applyRequest(Activity activity, ActivityCreateRequest request) {
        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setCoverUrl(request.getCoverUrl());
        activity.setLocation(request.getLocation());
        activity.setCategoryId(request.getCategoryId());
        activity.setMaxParticipants(request.getMaxParticipants());
        activity.setSignupStartTime(request.getRegistrationStartTime());
        activity.setSignupEndTime(request.getRegistrationEndTime());
        activity.setActivityStartTime(request.getStartTime());
        activity.setActivityEndTime(request.getEndTime());
    }

    private ActivityDetailResponse detail(Activity a) {
        return new ActivityDetailResponse(a.getId(), a.getCategoryId(), a.getTitle(), a.getDescription(),
                a.getCoverUrl(), a.getLocation(), a.getMaxParticipants(), a.getCurrentParticipants(), a.getStatus(),
                a.getSignupStartTime(), a.getSignupEndTime(), a.getActivityStartTime(), a.getActivityEndTime());
    }
}
