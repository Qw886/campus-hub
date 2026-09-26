package com.campushub.vo;


import lombok.Getter;

import java.util.List;

@Getter
public class ActivityPageResponse {

    private final List<ActivityListResponse> records;
    private final Long total;
    private final Long page;
    private final Long size;

    public ActivityPageResponse(List<ActivityListResponse> records,
                                Long total,
                                Long page,
                                Long size){
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
    }

}
