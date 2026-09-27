package com.campushub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("organizer_application")
public class OrganizerApplication {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String reason;
    private Integer status;
    private Long reviewerId;
    private String reviewReason;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
