package com.campushub.controller;

import com.campushub.common.Result;
import com.campushub.service.ActivityCheckinService;
import com.campushub.vo.UserProfileResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activities")
public class ActivityCheckinController {
    private final ActivityCheckinService checkinService;

    public ActivityCheckinController(ActivityCheckinService checkinService) {
        this.checkinService = checkinService;
    }

    @PostMapping("/{id}/checkins")
    public Result<Void> checkin(@PathVariable Long id,
                                @AuthenticationPrincipal UserProfileResponse user) {
        checkinService.checkin(user.getId(), id);
        return Result.success();
    }
}
