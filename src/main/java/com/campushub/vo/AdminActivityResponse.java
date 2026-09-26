package com.campushub.vo;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminActivityResponse {

    private final Long id;
    private final String title;
    private final Long organizerId;
    private final Integer status;
    private final LocalDateTime createdAt;

    public AdminActivityResponse(Long id ,
                                 String title,
                                 Long organizerId,
                                 Integer status,
                                 LocalDateTime  createdAt
    )
    {
        this.id = id;
        this.title = title;
        this.organizerId = organizerId;
        this.status = status;
        this.createdAt = createdAt;
    }


}
