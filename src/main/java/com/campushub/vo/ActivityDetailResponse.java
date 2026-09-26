package com.campushub.vo;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ActivityDetailResponse {

    /*字段：Long id/categoryId；String title/description/coverUrl/location；Integer maxParticipants/currentParticipants/status；
    LocalDateTime signupStartTime/signupEndTime/activityStartTime/activityEndTime。*/
    private final Long id;
    private final Long categoryId;
    private final String title;
    private final String description;
    private final String coverUrl;
    private final String location;
    private final Integer maxParticipants;
    private final Integer currentParticipants;
    private final Integer status;
    private final LocalDateTime signupStartTime;
    private final LocalDateTime signupEndTime;
    private final LocalDateTime activityStartTime;
    private final LocalDateTime activityEndTime;

    public ActivityDetailResponse(Long id,
    Long categoryId,
    String title,
    String description,
    String coverUrl,
    String location,
    Integer maxParticipants,
    Integer currentParticipants,
    Integer status,
    LocalDateTime signupStartTime,
    LocalDateTime signupEndTime,
    LocalDateTime activityStartTime,
    LocalDateTime activityEndTime)
    {
        this.id = id;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.coverUrl = coverUrl;
        this.location = location;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = currentParticipants;
        this.status = status;
        this.signupStartTime = signupStartTime;
        this.signupEndTime = signupEndTime;
        this.activityStartTime = activityStartTime;
        this.activityEndTime = activityEndTime;
    }


}
