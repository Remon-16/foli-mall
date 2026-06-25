package com.github.foli_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.foli_backend.common.BusinessException;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.constant.RoleConstants;
import com.github.foli_backend.context.UserContext;
import com.github.foli_backend.dto.request.LoginRequest;
import com.github.foli_backend.dto.request.RegisterRequest;
import com.github.foli_backend.dto.response.LoginVO;
import com.github.foli_backend.entity.FmUser;
import com.github.foli_backend.config.WebMvcConfig;
import com.github.foli_backend.interceptor.JwtInterceptor;
import com.github.foli_backend.service.FmUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        value = {JwtInterceptor.class, WebMvcConfig.class}))
@DisplayName("AuthController 集成测试")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean FmUserService userService;

    @BeforeEach
    void setUp() {
        UserContext.set(1L, RoleConstants.BUYER, "testUser");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    // ==================== POST /api/auth/register ====================

    @Test
    @DisplayName("should_return_200_when_register_with_valid_request")
    void shouldReturn200_whenRegisterWithValidRequest() throws Exception {
        FmUser user = new FmUser();
        user.setId(1L);
        user.setUsername("newuser");
        when(userService.register(anyString(), anyString(), anyString())).thenReturn(user);

        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("100000"));
    }

    @Test
    @DisplayName("should_return_400_when_register_with_blank_username")
    void shouldReturn400_whenRegisterWithBlankUsername() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ==================== POST /api/auth/login ====================

    @Test
    @DisplayName("should_return_200_with_token_when_login_with_valid_credentials")
    void shouldReturn200WithToken_whenLoginWithValidCredentials() throws Exception {
        LoginVO loginVO = new LoginVO("test-token", 1L, "buyer1", "buyer_nick", 0);
        when(userService.login(anyString(), anyString())).thenReturn(loginVO);

        LoginRequest req = new LoginRequest();
        req.setUsername("buyer1");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("100000"))
                .andExpect(jsonPath("$.data.token").value("test-token"));
    }

    @Test
    @DisplayName("should_return_error_when_login_with_invalid_credentials")
    void shouldReturnError_whenLoginWithInvalidCredentials() throws Exception {
        when(userService.login(anyString(), anyString()))
                .thenThrow(new BusinessException(BizCodeEnum.INVALID_CREDENTIALS));

        LoginRequest req = new LoginRequest();
        req.setUsername("nobody");
        req.setPassword("wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("201002"));
    }

    // ==================== GET /api/auth/me ====================

    @Test
    @DisplayName("should_return_200_with_user_info_when_authenticated")
    void shouldReturn200WithUserInfo_whenAuthenticated() throws Exception {
        FmUser user = new FmUser();
        user.setId(1L);
        user.setUsername("testUser");
        user.setNickname("Test Nick");
        user.setRole(0);
        when(userService.getCurrentUser(1L)).thenReturn(user);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("100000"));
    }
}
