package com.campushub.controller;

import com.campushub.common.Result;
import com.campushub.service.ActivitySignupService;
import com.campushub.vo.UserProfileResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activities")
public class ActivitySignupController {
    private final ActivitySignupService signupService;

    public ActivitySignupController(ActivitySignupService signupService) {
        this.signupService = signupService;
    }

    @PostMapping("/{id}/signups")
    public Result<Void> signup(@PathVariable Long id,
                               @AuthenticationPrincipal UserProfileResponse user) {
        signupService.signup(user.getId(), id);
        return Result.success();
    }

    @DeleteMapping("/{id}/signups")
    public Result<Void> cancel(@PathVariable Long id,
                               @AuthenticationPrincipal UserProfileResponse user) {
        signupService.cancel(user.getId(), id);
        return Result.success();
    }

}
