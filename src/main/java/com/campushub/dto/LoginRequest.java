package com.campushub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 20, message = "用户名不能超过20位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(max = 64, message = "密码不能超过64位")
    private String password;
}