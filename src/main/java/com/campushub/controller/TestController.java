package com.campushub.controller;

import com.campushub.common.Result;
import com.campushub.entity.SysUser;
import com.campushub.mapper.SysUserMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    private final SysUserMapper sysUserMapper;

    public TestController(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @GetMapping("/users")
    public Result<List<SysUser>> listUsers() {
        List<SysUser> users = sysUserMapper.selectList(null);
        return Result.success(users);
    }
}