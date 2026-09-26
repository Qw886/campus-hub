package com.campushub.controller;


import com.campushub.common.Result;
import com.campushub.dto.ActivityCreateRequest;
import com.campushub.dto.OrganizerActivityQueryRequest;
import com.campushub.dto.SignupQueryRequest;
import com.campushub.service.OrganizerActivityService;
import com.campushub.vo.ActivityDetailResponse;
import com.campushub.vo.OrganizerActivityPageResponse;
import com.campushub.vo.ParticipantPageResponse;
import com.campushub.vo.UserProfileResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizer/activities")
public class OrganizerActivityController {

    @Autowired
    private OrganizerActivityService organizerActivityService;

    @PostMapping
    public Result<Long> create(@Valid @RequestBody ActivityCreateRequest request , @AuthenticationPrincipal UserProfileResponse user){
        Long l = organizerActivityService.create(user.getId(), request);
        return Result.success(l);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody ActivityCreateRequest request,
                               @AuthenticationPrincipal UserProfileResponse user) {
        organizerActivityService.update(user.getId(), id, request);
        return Result.success();
    }

    @GetMapping
    public Result<OrganizerActivityPageResponse> listMine(
            @Valid @ModelAttribute OrganizerActivityQueryRequest request,
            @AuthenticationPrincipal UserProfileResponse user) {
        return Result.success(organizerActivityService.listMine(user.getId(), request));
    }

    @GetMapping("/{id}")
    public Result<ActivityDetailResponse> getMine(@PathVariable Long id,
                                                  @AuthenticationPrincipal UserProfileResponse user) {
        return Result.success(organizerActivityService.getMine(user.getId(), id));
    }

    @GetMapping("/{id}/signups")
    public Result<ParticipantPageResponse> listParticipants(
            @PathVariable Long id,
            @Valid @ModelAttribute SignupQueryRequest request,
            @AuthenticationPrincipal UserProfileResponse user) {
        return Result.success(organizerActivityService.listParticipants(user.getId(), id, request));
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                               @AuthenticationPrincipal UserProfileResponse user) {
        organizerActivityService.cancel(user.getId(), id);
        return Result.success();
    }

}
