package com.github.foli_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.constant.RoleConstants;
import com.github.foli_backend.context.UserContext;
import com.github.foli_backend.dto.response.OrderVO;
import com.github.foli_backend.config.WebMvcConfig;
import com.github.foli_backend.interceptor.JwtInterceptor;
import com.github.foli_backend.service.FmOrderService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = OrderController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        value = {JwtInterceptor.class, WebMvcConfig.class}))
@DisplayName("OrderController 集成测试")
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean FmOrderService orderService;

    @BeforeEach
    void setUp() {
        UserContext.set(1L, RoleConstants.BUYER, "buyer1");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("should_create_order_when_valid_request")
    void shouldCreateOrder_whenValidRequest() throws Exception {
        OrderVO vo = new OrderVO();
        vo.setId(1L);
        vo.setOrderNo("FO20260625000001");
        vo.setTotalAmount(BigDecimal.valueOf(99));
        when(orderService.createOrder(anyLong(), any())).thenReturn(List.of(vo));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receiverName\":\"Test\",\"receiverPhone\":\"13800138000\",\"receiverAddress\":\"Beijing\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("100000"))
                .andExpect(jsonPath("$.data[0].orderNo").value("FO20260625000001"));
    }

    @Test
    @DisplayName("should_return_paginated_orders_when_list_orders")
    void shouldReturnPaginatedOrders_whenListOrders() throws Exception {
        PageResult<OrderVO> page = new PageResult<>(1, 1, 10, Collections.emptyList());
        when(orderService.listBuyerOrders(anyLong(), anyInt(), anyInt(), isNull())).thenReturn(page);

        mockMvc.perform(get("/api/orders?page=1&pageSize=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("100000"));
    }

    @Test
    @DisplayName("should_return_order_detail_when_order_exists")
    void shouldReturnOrderDetail_whenOrderExists() throws Exception {
        OrderVO vo = new OrderVO();
        vo.setId(1L);
        when(orderService.getOrderDetail(1L)).thenReturn(vo);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("100000"));
    }

    @Test
    @DisplayName("should_pay_order_when_valid_state")
    void shouldPayOrder_whenValidState() throws Exception {
        mockMvc.perform(put("/api/orders/1/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("100000"));
    }

    @Test
    @DisplayName("should_cancel_order_when_valid_state")
    void shouldCancelOrder_whenValidState() throws Exception {
        mockMvc.perform(put("/api/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("100000"));
    }

    @Test
    @DisplayName("should_receive_order_when_valid_state")
    void shouldReceiveOrder_whenValidState() throws Exception {
        mockMvc.perform(put("/api/orders/1/receive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("100000"));
    }
}
