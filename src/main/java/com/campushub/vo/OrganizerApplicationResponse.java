package com.campushub.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class OrganizerApplicationResponse {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String reason;
    private Integer status;
    private String reviewReason;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
