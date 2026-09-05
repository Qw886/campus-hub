package com.campushub.controller;

import com.campushub.common.Result;
import com.campushub.dto.RegisterRequest;
import com.campushub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<Void> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        userService.register(request);
        return Result.success();
    }
}