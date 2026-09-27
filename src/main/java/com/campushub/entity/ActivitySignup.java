package com.campushub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("activity_signup")
public class ActivitySignup {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long activityId;
    private Integer status;
    private LocalDateTime signupTime;
    private LocalDateTime cancelTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
