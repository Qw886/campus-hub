package com.campushub.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrganizerApplicationPageResponse {
    private List<OrganizerApplicationResponse> records;
    private Long total;
    private Long page;
    private Long size;
}
