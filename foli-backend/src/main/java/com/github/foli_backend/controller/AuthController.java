package com.github.foli_backend.controller;

import com.github.foli_backend.annotation.RequireLogin;
import com.github.foli_backend.common.Result;
import com.github.foli_backend.context.UserContext;
import com.github.foli_backend.dto.request.LoginRequest;
import com.github.foli_backend.dto.request.RegisterRequest;
import com.github.foli_backend.dto.response.LoginVO;
import com.github.foli_backend.dto.response.UserVO;
import com.github.foli_backend.entity.FmUser;
import com.github.foli_backend.service.FmUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 认证管理控制器 / Auth management controller
 * 提供用户注册、登录、获取当前用户等认证相关接口
 * Provides endpoints for user registration, login, and current user retrieval
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理 Auth", description = "用户注册登录接口 User registration and login")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final FmUserService fmUserService;

    /**
     * 构造器注入 / Constructor injection
     * @param fmUserService 用户服务 / user service
     */
    public AuthController(FmUserService fmUserService) {
        this.fmUserService = fmUserService;
    }

    // ==================== 注册 / Register ====================

    /**
     * 用户注册 / User registration
     * 创建新用户账号，默认角色为买家，余额为0
     * Creates a new user account with default role BUYER and balance 0
     *
     * @param request 注册请求 / register request
     * @return 用户信息 / user info
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册 / User registration", description = "创建新用户账号 / Create new user account")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request: username={}", request.getUsername());
        FmUser user = fmUserService.register(
                request.getUsername(),
                request.getPassword(),
                request.getNickname()
        );
        return Result.success(UserVO.fromEntity(user));
    }

    // ==================== 登录 / Login ====================

    /**
     * 用户登录 / User login
     * 验证用户名密码，返回JWT令牌和用户基本信息
     * Verifies username and password, returns JWT token and basic user info
     *
     * @param request 登录请求 / login request
     * @return 登录响应（含JWT令牌）/ login response (with JWT token)
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录 / User login", description = "验证用户名密码并返回JWT令牌 / Verify credentials and return JWT token")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request: username={}", request.getUsername());
        LoginVO loginVO = fmUserService.login(request.getUsername(), request.getPassword());
        return Result.success(loginVO);
    }

    // ==================== 获取当前用户 / Get Current User ====================

    /**
     * 获取当前登录用户信息 / Get current user info
     * 需要登录认证，通过JWT令牌解析用户ID并返回用户信息（不含密码）
     * Requires login authentication; parses user ID from JWT token and returns user info (password excluded)
     *
     * @return 用户信息 / user info
     */
    @GetMapping("/me")
    @RequireLogin
    @Operation(summary = "获取当前用户信息 / Get current user info", description = "需要登录认证，返回当前登录用户的信息 / Requires login, returns current user info")
    public Result<UserVO> getMe() {
        Long userId = UserContext.getUserId();
        log.info("Get current user: userId={}", userId);
        FmUser user = fmUserService.getCurrentUser(userId);
        return Result.success(UserVO.fromEntity(user));
    }
}
