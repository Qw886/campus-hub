package com.campushub.vo;

import lombok.Getter;

@Getter
public class ActivityCategoryResponse {

    private final Long id;

    private final String name;

    public ActivityCategoryResponse(Long id , String name){
        this.id = id;
        this.name = name;
    }
}
