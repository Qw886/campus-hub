package com.campushub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("activity")
public class Activity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 标题、介绍、封面地址
    private String title;
    private String description;
    private String coverUrl;

    // 所属分类、发布者
    private Long categoryId;
    private Long organizerId;

    private String location;

    // 人数上限、当前报名人数
    private Integer maxParticipants;
    private Integer currentParticipants;

    // 报名时间
    private LocalDateTime signupStartTime;
    private LocalDateTime signupEndTime;

    // 活动时间
    private LocalDateTime activityStartTime;
    private LocalDateTime activityEndTime;

    // 1待审核 2报名中 3已结束 4已取消 5审核拒绝
    private Integer status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}