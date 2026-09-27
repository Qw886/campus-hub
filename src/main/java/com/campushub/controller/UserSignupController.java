package com.campushub.controller;

import com.campushub.common.Result;
import com.campushub.dto.SignupQueryRequest;
import com.campushub.service.ActivitySignupService;
import com.campushub.vo.MySignupPageResponse;
import com.campushub.vo.SignupStateResponse;
import com.campushub.vo.ActivityDetailResponse;
import com.campushub.vo.UserProfileResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;

@RestController
@RequestMapping("/api/users/me/signups")
public class UserSignupController {
    private final ActivitySignupService signupService;

    public UserSignupController(ActivitySignupService signupService) {
        this.signupService = signupService;
    }

    @GetMapping
    public Result<MySignupPageResponse> listMine(@Valid @ModelAttribute SignupQueryRequest request,
                                                 @AuthenticationPrincipal UserProfileResponse user) {
        return Result.success(signupService.listMine(user.getId(), request));
    }

    @GetMapping("/{activityId}/state")
    public Result<SignupStateResponse> state(@PathVariable Long activityId,
                                             @AuthenticationPrincipal UserProfileResponse user) {
        return Result.success(signupService.getState(user.getId(), activityId));
    }

    @GetMapping("/{activityId}/detail")
    public Result<ActivityDetailResponse> detail(@PathVariable Long activityId,
                                                 @AuthenticationPrincipal UserProfileResponse user) {
        return Result.success(signupService.getMineDetail(user.getId(), activityId));
    }
}
