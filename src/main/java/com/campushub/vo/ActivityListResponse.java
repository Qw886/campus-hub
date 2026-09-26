package com.campushub.vo;

import lombok.Getter;

import java.time.LocalDateTime;


@Getter
public class ActivityListResponse {

    private final Long id;
    private final Long categoryId;
    private final String title;
    private final String coverUrl;
    private final String location;
    private final LocalDateTime activityStartTime;
    private final Integer status;

    public ActivityListResponse(Long id, Long categoryId, String title, String coverUrl, String location, LocalDateTime activityStartTime, Integer status){
        this.id = id;
        this.categoryId = categoryId;
        this.title = title;
        this.coverUrl = coverUrl;
        this.location = location;
        this.activityStartTime = activityStartTime;
        this.status = status;

    }


}
