package com.campushub.vo;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OrganizerActivityResponse {
    private final Long id;
    private final String title;
    private final Integer status;
    private final Integer currentParticipants;
    private final Integer maxParticipants;
    private final LocalDateTime activityStartTime;
    private final String auditReason;

    public OrganizerActivityResponse(Long id, String title, Integer status,
                                     Integer currentParticipants, Integer maxParticipants,
                                     LocalDateTime activityStartTime, String auditReason) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.currentParticipants = currentParticipants;
        this.maxParticipants = maxParticipants;
        this.activityStartTime = activityStartTime;
        this.auditReason = auditReason;
    }
}
