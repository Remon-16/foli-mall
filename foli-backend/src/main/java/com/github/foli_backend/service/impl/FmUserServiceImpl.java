package com.github.foli_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.constant.RoleConstants;
import com.github.foli_backend.dto.response.LoginVO;
import com.github.foli_backend.entity.FmUser;
import com.github.foli_backend.mapper.FmUserMapper;
import com.github.foli_backend.service.FmUserService;
import com.github.foli_backend.utils.JwtUtil;
import com.github.foli_backend.utils.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FmUserServiceImpl implements FmUserService {

    private static final Logger log = LoggerFactory.getLogger(FmUserServiceImpl.class);

    private final FmUserMapper fmUserMapper;
    private final JwtUtil jwtUtil;

    public FmUserServiceImpl(FmUserMapper fmUserMapper, JwtUtil jwtUtil) {
        this.fmUserMapper = fmUserMapper;
        this.jwtUtil = jwtUtil;
    }

    // ==================== 注册 / Register ====================

    @Override
    public FmUser register(String username, String password, String nickname) {
        LambdaQueryWrapper<FmUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FmUser::getUsername, username);
        FmUser existUser = fmUserMapper.selectOne(queryWrapper);
        if (existUser != null) {
            log.warn("Registration failed: username '{}' already exists", username);
            BizCodeEnum.USERNAME_EXISTS.throwEx();
        }

        FmUser user = new FmUser();
        user.setUsername(username);
        user.setPassword(PasswordUtil.hash(password));
        user.setNickname(nickname != null ? nickname : username);
        user.setRole(RoleConstants.BUYER);
        user.setBalance(BigDecimal.ZERO);
        user.setStatus(1);

        fmUserMapper.insert(user);
        log.info("User registered: id={}, username={}", user.getId(), user.getUsername());
        return user;
    }

    // ==================== 登录 / Login ====================

    @Override
    public LoginVO login(String username, String password) {
        LambdaQueryWrapper<FmUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FmUser::getUsername, username);
        FmUser user = fmUserMapper.selectOne(queryWrapper);

        if (user == null) {
            log.warn("Login failed: username '{}' not found", username);
            BizCodeEnum.INVALID_CREDENTIALS.throwEx();
        }

        if (!PasswordUtil.verify(password, user.getPassword())) {
            log.warn("Login failed: incorrect password for username '{}'", username);
            BizCodeEnum.INVALID_CREDENTIALS.throwEx();
        }

        if (user.getStatus() == null || user.getStatus() == 0) {
            log.warn("Login failed: account '{}' is disabled", username);
            BizCodeEnum.ACCOUNT_DISABLED.throwEx();
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole(), user.getUsername());
        log.info("User logged in: id={}, username={}", user.getId(), user.getUsername());

        return new LoginVO(
                token,
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole()
        );
    }

    // ==================== 获取当前用户 / Get Current User ====================

    @Override
    public FmUser getCurrentUser(Long userId) {
        FmUser user = fmUserMapper.selectById(userId);
        if (user == null) {
            log.warn("User not found: id={}", userId);
            BizCodeEnum.USER_NOT_FOUND.throwEx();
        }
        return user;
    }
}
