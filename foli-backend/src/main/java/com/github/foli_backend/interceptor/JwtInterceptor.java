package com.github.foli_backend.interceptor;

import com.github.foli_backend.annotation.RequireLogin;
import com.github.foli_backend.annotation.RequireRole;
import com.github.foli_backend.common.Result;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.context.UserContext;
import com.github.foli_backend.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;

/**
 * JWT认证拦截器 JWT authentication interceptor
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtInterceptor.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireLogin classRequireLogin = handlerMethod.getBeanType().getAnnotation(RequireLogin.class);
        RequireLogin methodRequireLogin = handlerMethod.getMethodAnnotation(RequireLogin.class);

        boolean requireLogin = classRequireLogin != null || methodRequireLogin != null;

        if (!requireLogin) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeJson(response, 401, BizCodeEnum.BAD_REQUEST, "Missing or invalid Authorization header");
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            writeJson(response, 401, BizCodeEnum.BAD_REQUEST, "Invalid or expired token");
            return false;
        }

        Long userId = jwtUtil.getUserId(token);
        Integer role = jwtUtil.getRole(token);
        String username = jwtUtil.getUsername(token);
        UserContext.set(userId, role, username);

        RequireRole methodRequireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        RequireRole classRequireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);

        RequireRole requireRole = methodRequireRole != null ? methodRequireRole : classRequireRole;
        if (requireRole != null) {
            boolean allowed = Arrays.stream(requireRole.value()).anyMatch(r -> r == role);
            if (!allowed) {
                writeJson(response, 403, BizCodeEnum.FORBIDDEN, "Access denied: insufficient role permissions");
                return false;
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    private void writeJson(HttpServletResponse response, int httpStatus, BizCodeEnum bizCode, String message)
            throws IOException {
        response.setStatus(httpStatus);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(bizCode, message)));
    }
}
