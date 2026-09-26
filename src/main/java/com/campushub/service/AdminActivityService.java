package com.campushub.service;

import com.campushub.dto.ActivityAuditRequest;
import com.campushub.dto.AdminActivityQueryRequest;
import com.campushub.vo.ActivityDetailResponse;
import com.campushub.vo.AdminActivityPageResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

public interface AdminActivityService {


    AdminActivityPageResponse list(AdminActivityQueryRequest request);


    ActivityDetailResponse getDetail(Long id);

    void audit(Long adminId , Long activityId , ActivityAuditRequest request);


}
