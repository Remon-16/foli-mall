package com.github.foli_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求 DTO / Login request DTO
 * 用于接收用户登录时提交的凭证
 * Receives credentials submitted during user login
 */
public class LoginRequest {

    /** 用户名 / Username */
    @NotBlank(message = "用户名不能为空 / Username cannot be blank")
    private String username;

    /** 密码 / Password */
    @NotBlank(message = "密码不能为空 / Password cannot be blank")
    private String password;

    // ========== Getters and Setters ==========

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
