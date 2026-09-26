package com.campushub.service;

import com.campushub.dto.LoginRequest;
import com.campushub.dto.RegisterRequest;
import com.campushub.vo.LoginResponse;
import com.campushub.vo.UserProfileResponse;
import com.campushub.dto.UpdateProfileRequest;

public interface UserService {

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserProfileResponse getCurrentUser(Long userId);

    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);
}
