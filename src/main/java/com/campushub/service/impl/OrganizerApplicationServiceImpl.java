package com.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campushub.common.exception.BusinessException;
import com.campushub.dto.OrganizerApplicationRequest;
import com.campushub.dto.OrganizerApplicationReviewRequest;
import com.campushub.entity.OrganizerApplication;
import com.campushub.entity.SysUser;
import com.campushub.mapper.OrganizerApplicationMapper;
import com.campushub.mapper.SysUserMapper;
import com.campushub.service.OrganizerApplicationService;
import com.campushub.vo.OrganizerApplicationPageResponse;
import com.campushub.vo.OrganizerApplicationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrganizerApplicationServiceImpl implements OrganizerApplicationService {
    private final OrganizerApplicationMapper applicationMapper;
    private final SysUserMapper userMapper;

    public OrganizerApplicationServiceImpl(OrganizerApplicationMapper applicationMapper, SysUserMapper userMapper) {
        this.applicationMapper = applicationMapper;
        this.userMapper = userMapper;
    }

    @Override
    public OrganizerApplicationResponse getMine(Long userId) {
        OrganizerApplication application = applicationMapper.selectOne(new LambdaQueryWrapper<OrganizerApplication>()
                .eq(OrganizerApplication::getUserId, userId).orderByDesc(OrganizerApplication::getId).last("LIMIT 1"));
        return application == null ? null : toResponse(application, userMapper.selectById(userId));
    }

    @Override
    @Transactional
    public void apply(Long userId, OrganizerApplicationRequest request) {
        SysUser user = userMapper.selectByIdForUpdate(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        if (!"USER".equals(user.getRole())) throw new BusinessException(409, "当前账号不是学生账号或已经是组织者");
        Long pending = applicationMapper.selectCount(new LambdaQueryWrapper<OrganizerApplication>()
                .eq(OrganizerApplication::getUserId, userId).eq(OrganizerApplication::getStatus, 1));
        if (pending > 0) throw new BusinessException(409, "你已经提交过申请，请等待管理员审核");
        OrganizerApplication application = new OrganizerApplication();
        application.setUserId(userId);
        application.setReason(request.getReason().trim());
        application.setStatus(1);
        application.setCreatedAt(LocalDateTime.now());
        if (applicationMapper.insert(application) != 1) throw new BusinessException(500, "申请提交失败");
    }

    @Override
    public OrganizerApplicationPageResponse listPending(int page, int size) {
        Page<OrganizerApplication> result = applicationMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<OrganizerApplication>().eq(OrganizerApplication::getStatus, 1)
                        .orderByAsc(OrganizerApplication::getCreatedAt));
        List<Long> userIds = result.getRecords().stream().map(OrganizerApplication::getUserId).distinct().toList();
        Map<Long, SysUser> users = new HashMap<>();
        if (!userIds.isEmpty()) userMapper.selectBatchIds(userIds).forEach(user -> users.put(user.getId(), user));
        List<OrganizerApplicationResponse> records = result.getRecords().stream()
                .map(application -> toResponse(application, users.get(application.getUserId()))).toList();
        return new OrganizerApplicationPageResponse(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional
    public void review(Long adminId, Long applicationId, OrganizerApplicationReviewRequest request) {
        OrganizerApplication application = applicationMapper.selectById(applicationId);
        if (application == null) throw new BusinessException(404, "申请不存在");
        if (!Integer.valueOf(1).equals(application.getStatus())) throw new BusinessException(409, "该申请已经处理");
        if (!request.getApproved() && !StringUtils.hasText(request.getReason())) {
            throw new BusinessException(400, "拒绝时必须填写原因");
        }
        int rows = applicationMapper.update(null, new LambdaUpdateWrapper<OrganizerApplication>()
                .eq(OrganizerApplication::getId, applicationId)
                .eq(OrganizerApplication::getStatus, 1)
                .set(OrganizerApplication::getStatus, request.getApproved() ? 2 : 3)
                .set(OrganizerApplication::getReviewerId, adminId)
                .set(OrganizerApplication::getReviewReason,
                        StringUtils.hasText(request.getReason()) ? request.getReason().trim() : "申请通过")
                .set(OrganizerApplication::getReviewedAt, LocalDateTime.now()));
        if (rows != 1) throw new BusinessException(409, "申请状态已变化，请刷新");
        if (request.getApproved()) {
            int userRows = userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                    .eq(SysUser::getId, application.getUserId())
                    .eq(SysUser::getRole, "USER")
                    .set(SysUser::getRole, "ORGANIZER"));
            if (userRows != 1) throw new BusinessException(409, "用户角色已变化，请刷新");
        }
    }

    private OrganizerApplicationResponse toResponse(OrganizerApplication application, SysUser user) {
        return new OrganizerApplicationResponse(application.getId(), application.getUserId(),
                user == null ? null : user.getUsername(), user == null ? null : user.getNickname(),
                application.getReason(), application.getStatus(), application.getReviewReason(),
                application.getCreatedAt(), application.getReviewedAt());
    }
}
