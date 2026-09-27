package com.campushub.vo;

import lombok.Getter;

import java.util.List;

@Getter
public class ParticipantPageResponse {
    private final List<ParticipantResponse> records;
    private final Long total;
    private final Long page;
    private final Long size;

    public ParticipantPageResponse(List<ParticipantResponse> records,
                                   Long total, Long page, Long size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
    }
}
