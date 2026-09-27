package com.campushub.vo;

import lombok.Getter;

import java.util.List;

@Getter
public class MySignupPageResponse {
    private final List<MySignupResponse> records;
    private final Long total;
    private final Long page;
    private final Long size;

    public MySignupPageResponse(List<MySignupResponse> records,
                                Long total, Long page, Long size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
    }
}
