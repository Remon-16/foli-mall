package com.github.foli_backend.utils;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码工具类 基于Hutool BCrypt实现
 * Password utility using Hutool BCrypt
 */
public class PasswordUtil {

    /** 加密密码 Hash password */
    public static String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword);
    }

    /** 验证密码 Verify password */
    public static boolean verify(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }
}
