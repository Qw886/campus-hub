package com.campushub.service;

import com.campushub.dto.SignupQueryRequest;
import com.campushub.vo.MySignupPageResponse;
import com.campushub.vo.ActivityDetailResponse;
import com.campushub.vo.SignupStateResponse;

public interface ActivitySignupService {
    void signup(Long userId, Long activityId);
    void cancel(Long userId, Long activityId);
    MySignupPageResponse listMine(Long userId, SignupQueryRequest request);
    SignupStateResponse getState(Long userId, Long activityId);
    ActivityDetailResponse getMineDetail(Long userId, Long activityId);
}
