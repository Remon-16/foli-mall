package com.github.foli_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 注册请求 DTO / Register request DTO
 * 用于接收用户注册时提交的信息
 * Receives user registration information
 */
public class RegisterRequest {

    /** 用户名 / Username */
    @NotBlank(message = "用户名不能为空 / Username cannot be blank")
    @Size(min = 3, max = 50, message = "用户名长度为3-50个字符 / Username must be 3-50 characters")
    private String username;

    /** 密码 / Password */
    @NotBlank(message = "密码不能为空 / Password cannot be blank")
    @Size(min = 6, max = 100, message = "密码长度为6-100个字符 / Password must be 6-100 characters")
    private String password;

    /** 昵称 / Nickname */
    @Size(max = 50, message = "昵称最长50个字符 / Nickname maximum 50 characters")
    private String nickname;

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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
