package com.campushub.controller;


import com.campushub.common.Result;
import com.campushub.dto.ActivityQueryRequest;
import com.campushub.service.ActivityService;
import com.campushub.vo.ActivityCategoryResponse;
import com.campushub.vo.ActivityDetailResponse;
import com.campushub.vo.ActivityListResponse;
import com.campushub.vo.ActivityPageResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;



    @GetMapping
    public Result<ActivityPageResponse> list(@Valid @ModelAttribute ActivityQueryRequest request){
        return Result.success(activityService.listActivities(request));
    }

    @GetMapping("/{id}")
    public Result<ActivityDetailResponse> getPublicDetail(@PathVariable Long id){
        ActivityDetailResponse publicDetail = activityService.getPublicDetail(id);
        return Result.success(publicDetail);
    }

}
