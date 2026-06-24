package com.github.foli_backend.service;

import com.github.foli_backend.dto.response.LoginVO;
import com.github.foli_backend.entity.FmUser;

/**
 * 用户服务接口 / User service interface
 * 提供用户注册、登录、信息查询等核心业务方法
 * Provides core business methods: registration, login, user info query
 */
public interface FmUserService {

    /**
     * 用户注册 / Register a new user
     * 哈希密码、设置默认角色(BUYER)、默认余额0、保存用户
     * Hashes password, sets default role (BUYER), default balance 0, saves user
     *
     * @param username 用户名 / username
     * @param password 明文密码 / plaintext password
     * @param nickname 昵称 / nickname (optional)
     * @return 创建的用户实体 / the created user entity
     */
    FmUser register(String username, String password, String nickname);

    /**
     * 用户登录 / User login
     * 验证用户名密码、检查账号状态、生成JWT令牌
     * Verifies credentials, checks account status, generates JWT token
     *
     * @param username 用户名 / username
     * @param password 明文密码 / plaintext password
     * @return 登录响应VO（含JWT令牌）/ login response VO (including JWT token)
     */
    LoginVO login(String username, String password);

    /**
     * 获取当前用户信息 / Get current user info
     * 根据用户ID查询用户，不存在则抛出业务异常
     * Finds user by ID, throws BusinessException if not found
     *
     * @param userId 用户ID / user ID
     * @return 用户实体 / user entity
     */
    FmUser getCurrentUser(Long userId);
}
