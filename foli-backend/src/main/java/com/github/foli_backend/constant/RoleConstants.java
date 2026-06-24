package com.github.foli_backend.constant;

/**
 * 角色常量 Role constants
 */
public class RoleConstants {

    /** 普通买家 Buyer */
    public static final int BUYER = 0;

    /** 电商卖家 Seller */
    public static final int SELLER = 1;

    /** 后台管理员 Admin */
    public static final int ADMIN = 2;

    public static String getName(int role) {
        return switch (role) {
            case BUYER -> "BUYER";
            case SELLER -> "SELLER";
            case ADMIN -> "ADMIN";
            default -> "UNKNOWN";
        };
    }
}
