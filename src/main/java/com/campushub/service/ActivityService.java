package com.campushub.service;

import com.campushub.dto.ActivityQueryRequest;
import com.campushub.vo.ActivityDetailResponse;
import com.campushub.vo.ActivityListResponse;
import com.campushub.vo.ActivityPageResponse;

import java.util.List;

public interface ActivityService {
    ActivityPageResponse listActivities(ActivityQueryRequest request);

    ActivityDetailResponse getPublicDetail(Long id);

}
