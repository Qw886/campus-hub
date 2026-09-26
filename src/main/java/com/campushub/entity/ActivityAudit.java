package com.campushub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("activity_audit")
public class ActivityAudit {

   /* 真实字段：Long id/activityId/adminId；Integer auditStatus；String reason；
    LocalDateTime createdAt。@TableName("activity_audit")，id自增。
*/

    @TableId(value = "id" , type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long adminId;
    private Integer auditStatus;
    private String reason;
    private LocalDateTime createdAt;

}
