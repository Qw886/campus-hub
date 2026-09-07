package com.campushub.controller;

import com.campushub.common.Result;
import com.campushub.dto.RegisterRequest;
import com.campushub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.campushub.dto.LoginRequest;
import com.campushub.vo.LoginResponse;
import com.campushub.common.exception.BusinessException;
import com.campushub.service.JwtService;
import com.campushub.vo.UserProfileResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(
            UserService userService,
            JwtService jwtService
    ) {
        this.userService = userService;
        this.jwtService = jwtService;
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
            @RequestHeader(value = "Authorization", required = false)
            String authorization
    ) {
        if (authorization == null
                || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new BusinessException(401, "请携带有效的登录凭证");
        }

        String token = authorization.substring(7).trim();

        if (token.isEmpty()) {
            throw new BusinessException(401, "登录凭证不能为空");
        }

        Long userId = jwtService.parseUserId(token);

        UserProfileResponse response = userService.getCurrentUser(userId);

        return Result.success(response);
    }


}