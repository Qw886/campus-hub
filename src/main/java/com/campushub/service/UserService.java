package com.campushub.service;

import com.campushub.dto.LoginRequest;
import com.campushub.dto.RegisterRequest;
import com.campushub.vo.LoginResponse;

public interface UserService {

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}