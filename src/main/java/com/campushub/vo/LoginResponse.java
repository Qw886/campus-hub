package com.campushub.vo;

import lombok.Getter;

@Getter
public class LoginResponse {

    private final Long id;
    private final String username;
    private final String nickname;
    private final String role;

    public LoginResponse(
            Long id,
            String username,
            String nickname,
            String role
    ) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.role = role;
    }
}