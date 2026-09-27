package com.campushub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("activity_checkin")
public class ActivityCheckin {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long userId;
    private Long signupId;
    private LocalDateTime checkinTime;
    private LocalDateTime createdAt;
}
