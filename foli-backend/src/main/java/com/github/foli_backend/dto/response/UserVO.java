package com.github.foli_backend.dto.response;

import com.github.foli_backend.entity.FmUser;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户信息 VO / User info VO
 * 用于返回用户个人信息（不含密码）
 * Returns user profile info (password excluded)
 */
public class UserVO {

    /** 用户ID / User ID */
    private Long id;

    /** 用户名 / Username */
    private String username;

    /** 昵称 / Nickname */
    private String nickname;

    /** 手机号 / Phone */
    private String phone;

    /** 邮箱 / Email */
    private String email;

    /** 头像 / Avatar */
    private String avatar;

    /** 余额 / Balance */
    private BigDecimal balance;

    /** 角色 0=买家 1=卖家 2=管理员 / Role 0=BUYER 1=SELLER 2=ADMIN */
    private Integer role;

    /** 状态 0=禁用 1=正常 / Status 0=disabled 1=active */
    private Integer status;

    /** 创建时间 / Create time */
    private LocalDateTime createTime;

    // ========== Constructors ==========

    public UserVO() {}

    public UserVO(Long id, String username, String nickname, String phone, String email,
                  String avatar, BigDecimal balance, Integer role, Integer status,
                  LocalDateTime createTime) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.phone = phone;
        this.email = email;
        this.avatar = avatar;
        this.balance = balance;
        this.role = role;
        this.status = status;
        this.createTime = createTime;
    }

    // ========== Static Factory Method ==========

    /**
     * 从实体转换 VO（排除密码字段）/ Convert entity to VO (excludes password)
     * @param user FmUser entity
     * @return UserVO instance
     */
    public static UserVO fromEntity(FmUser user) {
        if (user == null) {
            return null;
        }
        // 不拷贝 password 字段 / Do NOT copy the password field
        return new UserVO(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getPhone(),
                user.getEmail(),
                user.getAvatar(),
                user.getBalance(),
                user.getRole(),
                user.getStatus(),
                user.getCreateTime()
        );
    }

    // ========== Getters and Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
