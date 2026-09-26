package com.campushub.controller;

import com.campushub.common.Result;
import com.campushub.dto.RegisterRequest;
import com.campushub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.campushub.dto.LoginRequest;
import com.campushub.dto.UpdateProfileRequest;
import com.campushub.vo.LoginResponse;
import com.campushub.vo.UserProfileResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(
            UserService userService
    ) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<Void> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        userService.register(request);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = userService.login(request);

        return Result.success(response);
    }

    @GetMapping("/me")
    public Result<UserProfileResponse> me(
            @AuthenticationPrincipal UserProfileResponse user
    ) {

        return Result.success(user);
    }

    @PutMapping("/me")
    public Result<UserProfileResponse> updateMe(@Valid @RequestBody UpdateProfileRequest request,
                                                 @AuthenticationPrincipal UserProfileResponse user) {
        return Result.success(userService.updateProfile(user.getId(), request));
    }


}
