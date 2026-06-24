package com.github.foli_backend.annotation;

import java.lang.annotation.*;

/**
 * 标记接口需要登录验证 Mark endpoint as requiring login
 * 未登录用户访问带此注解的接口将返回401
 * Unauthenticated access to annotated endpoints returns 401
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireLogin {
}
