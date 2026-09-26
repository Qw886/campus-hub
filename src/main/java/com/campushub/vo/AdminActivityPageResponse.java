package com.campushub.vo;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminActivityPageResponse {
    private List<AdminActivityResponse> records;
    private Long total;
    private Integer page;
    private Integer size;
}
