package com.github.foli_backend.context;

/**
 * 用户上下文 基于ThreadLocal存储当前请求的用户信息
 * User context - stores current request's user info via ThreadLocal
 */
public class UserContext {

    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<Integer> roleHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> usernameHolder = new ThreadLocal<>();

    public static void set(Long userId, Integer role, String username) {
        userIdHolder.set(userId);
        roleHolder.set(role);
        usernameHolder.set(username);
    }

    public static Long getUserId() {
        return userIdHolder.get();
    }

    public static Integer getRole() {
        return roleHolder.get();
    }

    public static String getUsername() {
        return usernameHolder.get();
    }

    /** 清除 ThreadLocal 防止内存泄漏 Clear ThreadLocal to prevent memory leak */
    public static void clear() {
        userIdHolder.remove();
        roleHolder.remove();
        usernameHolder.remove();
    }
}
