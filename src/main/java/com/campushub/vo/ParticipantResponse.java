package com.campushub.vo;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ParticipantResponse {
    private final Long signupId;
    private final Long userId;
    private final String username;
    private final String nickname;
    private final Integer signupStatus;
    private final LocalDateTime signupTime;
    private final Boolean checkedIn;

    public ParticipantResponse(Long signupId, Long userId, String username, String nickname,
                                Integer signupStatus, LocalDateTime signupTime, Boolean checkedIn) {
        this.signupId = signupId;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.signupStatus = signupStatus;
        this.signupTime = signupTime;
        this.checkedIn = checkedIn;
    }
}
