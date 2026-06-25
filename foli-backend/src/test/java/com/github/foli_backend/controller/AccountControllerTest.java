package com.github.foli_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.constant.RoleConstants;
import com.github.foli_backend.context.UserContext;
import com.github.foli_backend.dto.response.BalanceLogVO;
import com.github.foli_backend.config.WebMvcConfig;
import com.github.foli_backend.interceptor.JwtInterceptor;
import com.github.foli_backend.service.AccountService;
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

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AccountController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        value = {JwtInterceptor.class, WebMvcConfig.class}))
@DisplayName("AccountController 集成测试")
class AccountControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AccountService accountService;

    @BeforeEach
    void setUp() {
        UserContext.set(1L, RoleConstants.BUYER, "buyer1");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("should_return_balance")
    void shouldReturnBalance() throws Exception {
        when(accountService.getBalance(1L)).thenReturn(BigDecimal.valueOf(500));

        mockMvc.perform(get("/api/account/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("100000"))
                .andExpect(jsonPath("$.data").value(500));
    }

    @Test
    @DisplayName("should_recharge_successfully")
    void shouldRechargeSuccessfully() throws Exception {
        mockMvc.perform(post("/api/account/recharge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("100000"));
    }

    @Test
    @DisplayName("should_return_balance_logs_paginated")
    void shouldReturnBalanceLogsPaginated() throws Exception {
        PageResult<BalanceLogVO> page = new PageResult<>(0, 1, 20, Collections.emptyList());
        when(accountService.getBalanceLogs(anyLong(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/account/balance-logs?page=1&pageSize=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("100000"));
    }
}
