package com.campushub.controller;


import com.campushub.common.Result;
import com.campushub.dto.ActivityAuditRequest;
import com.campushub.dto.AdminActivityQueryRequest;
import com.campushub.service.AdminActivityService;
import com.campushub.vo.ActivityDetailResponse;
import com.campushub.vo.AdminActivityPageResponse;
import com.campushub.vo.UserProfileResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/activities")
public class AdminActivityController {

    private final AdminActivityService adminActivityService;

    public AdminActivityController(AdminActivityService adminActivityService) {
        this.adminActivityService = adminActivityService;
    }

    @GetMapping
    public Result<AdminActivityPageResponse> list(
            @Valid @ModelAttribute AdminActivityQueryRequest request
    ) {
        AdminActivityPageResponse response =
                adminActivityService.list(request);

        return Result.success(response);
    }

    @GetMapping("/{id}")
    public Result<ActivityDetailResponse> getDetail(@PathVariable Long id) {

        ActivityDetailResponse response =
                adminActivityService.getDetail(id);

        return Result.success(response);
    }

  /*  @PostMapping("/{id}/audit")
    public Result<ActivityAuditRequest> audit(@Valid @RequestBody Long adminId , Long activityId , ActivityAuditRequest request){

    }*/

    @PostMapping("/{id}/audit")
    public Result<Void> audit(
            @PathVariable Long id,
            @Valid @RequestBody ActivityAuditRequest request,
            @AuthenticationPrincipal UserProfileResponse user
    ) {
        adminActivityService.audit(user.getId(), id, request);
        return Result.success();
    }

}
