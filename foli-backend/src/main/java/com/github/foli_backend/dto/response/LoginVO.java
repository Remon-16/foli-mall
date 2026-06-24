package com.github.foli_backend.dto.response;

/**
 * 登录响应 VO / Login response VO
 * 登录成功后返回给前端的用户身份信息
 * Returns user identity info to frontend after successful login
 */
public class LoginVO {

    /** JWT令牌 / JWT token */
    private String token;

    /** 用户ID / User ID */
    private Long userId;

    /** 用户名 / Username */
    private String username;

    /** 昵称 / Nickname */
    private String nickname;

    /** 角色 0=买家 1=卖家 2=管理员 / Role 0=BUYER 1=SELLER 2=ADMIN */
    private Integer role;

    // ========== Constructors ==========

    public LoginVO() {}

    public LoginVO(String token, Long userId, String username, String nickname, Integer role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.role = role;
    }

    // ========== Getters and Setters ==========

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }
}
