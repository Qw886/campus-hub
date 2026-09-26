package com.campushub.vo;

import lombok.Getter;

@Getter
public class UserProfileResponse {

    private final Long id;
    private final String username;
    private final String nickname;
    private final String role;
    private final String phone;
    private final String avatar;

    public UserProfileResponse(
            Long id,
            String username,
            String nickname,
            String role,
            String phone,
            String avatar
    ) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.role = role;
        this.phone = phone;
        this.avatar = avatar;
    }
}
