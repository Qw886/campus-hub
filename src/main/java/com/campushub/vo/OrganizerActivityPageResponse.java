package com.campushub.vo;

import lombok.Getter;

import java.util.List;

@Getter
public class OrganizerActivityPageResponse {
    private final List<OrganizerActivityResponse> records;
    private final Long total;
    private final Long page;
    private final Long size;

    public OrganizerActivityPageResponse(List<OrganizerActivityResponse> records,
                                         Long total, Long page, Long size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
    }
}
