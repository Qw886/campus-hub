package com.campushub.vo;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MySignupResponse {
    private final Long signupId;
    private final Long activityId;
    private final String title;
    private final String location;
    private final Integer activityStatus;
    private final Integer signupStatus;
    private final LocalDateTime signupTime;
    private final LocalDateTime cancelTime;
    private final LocalDateTime activityStartTime;
    private final Boolean checkedIn;

    public MySignupResponse(Long signupId, Long activityId, String title, String location,
                            Integer activityStatus, Integer signupStatus,
                            LocalDateTime signupTime, LocalDateTime cancelTime,
                            LocalDateTime activityStartTime, Boolean checkedIn) {
        this.signupId = signupId;
        this.activityId = activityId;
        this.title = title;
        this.location = location;
        this.activityStatus = activityStatus;
        this.signupStatus = signupStatus;
        this.signupTime = signupTime;
        this.cancelTime = cancelTime;
        this.activityStartTime = activityStartTime;
        this.checkedIn = checkedIn;
    }
}
