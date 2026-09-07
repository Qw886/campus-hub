package com.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campushub.common.exception.BusinessException;
import com.campushub.dto.LoginRequest;
import com.campushub.dto.RegisterRequest;
import com.campushub.entity.SysUser;
import com.campushub.mapper.SysUserMapper;
import com.campushub.service.UserService;
import com.campushub.vo.LoginResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.campushub.service.JwtService;
import com.campushub.vo.UserProfileResponse;

@Service
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserServiceImpl(
            SysUserMapper sysUserMapper,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public void register(RegisterRequest request) {
        // 1. 查询用户名是否已经存在
        LambdaQueryWrapper<SysUser> queryWrapper =
                new LambdaQueryWrapper<>();

        queryWrapper.eq(
                SysUser::getUsername,
                request.getUsername()
        );

        Long count = sysUserMapper.selectCount(queryWrapper);

        if (count > 0) {
            throw new BusinessException(409, "用户名已存在");
        }

        // 2. 将注册参数转换成数据库实体
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());

        // 3. 加密密码
        String encodedPassword =
                passwordEncoder.encode(request.getPassword());

        user.setPassword(encodedPassword);

        // 4. 没填写昵称时，使用用户名作为昵称
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        } else {
            user.setNickname(request.getUsername());
        }

        // 5. 手机号是可选字段
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }

        // 6. 角色和状态只能由后端决定
        user.setRole("USER");
        user.setStatus(1);

        // 7. 插入数据库
        int affectedRows = sysUserMapper.insert(user);

        if (affectedRows != 1) {
            throw new BusinessException(500, "注册失败");
        }
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 根据用户名查询用户
        LambdaQueryWrapper<SysUser> queryWrapper =
                new LambdaQueryWrapper<>();

        queryWrapper.eq(
                SysUser::getUsername,
                request.getUsername()
        );

        SysUser user = sysUserMapper.selectOne(queryWrapper);

        // 2. 用户不存在
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 3. 验证密码
        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!passwordMatches) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 4. 检查账号状态
        if (user.getStatus() != 1) {
            throw new BusinessException(403, "账号已禁用");
        }

        // 5. 身份校验通过后，生成登录令牌
        String token = jwtService.generateToken(user.getId());

        // 6. 返回用户信息和令牌
        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole(),
                token
        );
    }

    @Override
    public UserProfileResponse getCurrentUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(401, "用户不存在，请重新登录");
        }

        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(403, "账号已禁用");
        }

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole()
        );
    }
}