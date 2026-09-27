package com.campushub.controller;

import com.campushub.common.Result;
import com.campushub.dto.OrganizerApplicationRequest;
import com.campushub.dto.OrganizerApplicationReviewRequest;
import com.campushub.service.OrganizerApplicationService;
import com.campushub.vo.OrganizerApplicationPageResponse;
import com.campushub.vo.OrganizerApplicationResponse;
import com.campushub.vo.UserProfileResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrganizerApplicationController {
    private final OrganizerApplicationService service;

    public OrganizerApplicationController(OrganizerApplicationService service) {
        this.service = service;
    }

    @GetMapping("/api/users/me/organizer-application")
    public Result<OrganizerApplicationResponse> mine(@AuthenticationPrincipal UserProfileResponse user) {
        return Result.success(service.getMine(user.getId()));
    }

    @PostMapping("/api/users/me/organizer-application")
    public Result<Void> apply(@Valid @RequestBody OrganizerApplicationRequest request,
                              @AuthenticationPrincipal UserProfileResponse user) {
        service.apply(user.getId(), request);
        return Result.success();
    }

    @GetMapping("/api/admin/organizer-applications")
    public Result<OrganizerApplicationPageResponse> pending(@RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "10") int size) {
        if (page < 1 || size < 1 || size > 50) throw new com.campushub.common.exception.BusinessException(400, "分页参数不合法");
        return Result.success(service.listPending(page, size));
    }

    @PostMapping("/api/admin/organizer-applications/{id}/review")
    public Result<Void> review(@PathVariable Long id,
                               @Valid @RequestBody OrganizerApplicationReviewRequest request,
                               @AuthenticationPrincipal UserProfileResponse user) {
        service.review(user.getId(), id, request);
        return Result.success();
    }
}
