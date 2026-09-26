package com.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campushub.common.exception.BusinessException;
import com.campushub.dto.ActivityAuditRequest;
import com.campushub.dto.AdminActivityQueryRequest;
import com.campushub.entity.Activity;
import com.campushub.entity.ActivityAudit;
import com.campushub.mapper.ActivityAuditMapper;
import com.campushub.mapper.ActivityMapper;
import com.campushub.service.AdminActivityService;
import com.campushub.vo.ActivityDetailResponse;
import com.campushub.vo.AdminActivityPageResponse;
import com.campushub.vo.AdminActivityResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminActivityServiceImpl implements AdminActivityService {
    private final ActivityMapper activityMapper;
    private final ActivityAuditMapper activityAuditMapper;

    public AdminActivityServiceImpl(
            ActivityMapper activityMapper,
            ActivityAuditMapper activityAuditMapper
    ) {
        this.activityMapper = activityMapper;
        this.activityAuditMapper = activityAuditMapper;
    }

    @Override
    public AdminActivityPageResponse list(AdminActivityQueryRequest request) {

        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        if (request.getStatus() != null){
            wrapper.eq(
                    Activity::getStatus,
                    request.getStatus()
            );
        }

        wrapper.orderByDesc(Activity::getId);
        Page<Activity> page = new Page<>(
                request.getPage(),
                request.getSize()
        );

        Page<Activity> result = activityMapper.selectPage(page , wrapper);


        List<AdminActivityResponse> records = result.getRecords()
                .stream()
                .map(activity -> {
                    AdminActivityResponse response = new AdminActivityResponse(
                            activity.getId(),
                            activity.getTitle(),
                            activity.getOrganizerId(),
                            activity.getStatus(),
                            activity.getCreatedAt()
                            );

                    return response;
                })
                .toList();

        AdminActivityPageResponse response = new AdminActivityPageResponse();
        response.setPage(request.getPage());
        response.setSize(request.getSize());
        response.setRecords(records);
        response.setTotal(result.getTotal());
        return response;
    }

    @Override
    public ActivityDetailResponse getDetail(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "活动编号必须大于0");
        }

        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }

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

    @Override
    @Transactional
    public void audit(
            Long adminId,
            Long activityId,
            ActivityAuditRequest request
    ) {

        // =========================
        // 1. 查询活动
        // =========================
        Activity activity = activityMapper.selectById(activityId);

        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }


        // =========================
        // 2. 必须是待审核状态
        // Activity.status = 1 才代表待审核
        // =========================
        if (!Integer.valueOf(1).equals(activity.getStatus())) {
            throw new BusinessException(409, "该活动不是待审核状态");
        }


        // =========================
        // 3. 获取管理员审核结果
        // auditStatus:
        // 1 = 通过
        // 2 = 拒绝
        // =========================
        Integer auditStatus = request.getAuditStatus();

        if (!Integer.valueOf(1).equals(auditStatus)
                && !Integer.valueOf(2).equals(auditStatus)) {
            throw new BusinessException(400, "审核结果只能是1或2");
        }

        Integer newStatus;


        // =========================
        // 4. 审核通过
        // =========================
        if (auditStatus == 1) {

            // 报名截止时间必须还在未来
            if (activity.getSignupEndTime() == null
                    || !activity.getSignupEndTime()
                    .isAfter(LocalDateTime.now())) {

                throw new BusinessException(
                        409,
                        "报名截止时间已过，不能通过审核"
                );
            }

            // 审核通过后
            // Activity.status = 2
            newStatus = 2;
        }


        // =========================
        // 5. 审核拒绝
        // =========================
        else {

            // 拒绝必须填写原因
            if (request.getReason() == null
                    || request.getReason().isBlank()) {

                throw new BusinessException(
                        400,
                        "拒绝审核时必须填写原因"
                );
            }

            // 审核拒绝后
            // Activity.status = 5
            newStatus = 5;
        }


        // =========================
        // 6. 更新 activity
        //
        // 注意：
        // SQL里面要求
        // id = activityId
        // AND status = 1
        // =========================
        int rows = activityMapper.updateAuditStatus(
                activityId,
                newStatus
        );


        // 没有恰好修改1行
        // 说明可能已经被其他管理员审核
        if (rows != 1) {
            throw new BusinessException(
                    409,
                    "活动已被其他管理员审核，请刷新后重试"
            );
        }


        // =========================
        // 7. 创建审核记录
        // =========================
        ActivityAudit audit = new ActivityAudit();

        audit.setActivityId(activityId);

        // 真实的当前管理员ID
        audit.setAdminId(adminId);

        // 1通过 / 2拒绝
        audit.setAuditStatus(auditStatus);

        audit.setReason(request.getReason());

        audit.setCreatedAt(LocalDateTime.now());


        // =========================
        // 8. 插入 activity_audit
        // =========================
        int auditRows = activityAuditMapper.insert(audit);

        if (auditRows != 1) {
            throw new BusinessException(500, "审核记录保存失败");
        }
    }
}
