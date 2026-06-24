package com.github.foli_backend.annotation;

import com.github.foli_backend.constant.RoleConstants;

import java.lang.annotation.*;

/**
 * 标记接口需要特定角色才能访问 Mark endpoint as requiring a specific role
 * 角色不匹配的用户返回403 Forbidden
 * Role mismatch returns 403 Forbidden
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /** 允许访问的角色列表 0=BUYER 1=SELLER 2=ADMIN Allowed roles */
    int[] value() default {RoleConstants.BUYER, RoleConstants.SELLER, RoleConstants.ADMIN};
}
