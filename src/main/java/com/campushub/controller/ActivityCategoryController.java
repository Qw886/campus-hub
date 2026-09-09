package com.campushub.controller;


import com.campushub.common.Result;
import com.campushub.service.ActivityCategoryService;
import com.campushub.vo.ActivityCategoryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activity-categories")
public class ActivityCategoryController {

    private final ActivityCategoryService activityCategoryService;

    public ActivityCategoryController(ActivityCategoryService activityCategoryService){
        this.activityCategoryService = activityCategoryService;
    }

    @GetMapping
    public Result<List<ActivityCategoryResponse>> list() {
        List<ActivityCategoryResponse> activityCategoryResponses = activityCategoryService.listEnabledCategories();

        return Result.success(activityCategoryResponses);
    }


}
