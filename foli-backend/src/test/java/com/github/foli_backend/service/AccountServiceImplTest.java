package com.github.foli_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.foli_backend.common.BusinessException;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.constant.BalanceLogTypeEnum;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.dto.response.BalanceLogVO;
import com.github.foli_backend.entity.FmBalanceLog;
import com.github.foli_backend.entity.FmUser;
import com.github.foli_backend.helper.TestDataFactory;
import com.github.foli_backend.mapper.FmBalanceLogMapper;
import com.github.foli_backend.mapper.FmUserMapper;
import com.github.foli_backend.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountServiceImpl 单元测试")
class AccountServiceImplTest {

    @Mock FmUserMapper fmUserMapper;
    @Mock FmBalanceLogMapper fmBalanceLogMapper;

    @InjectMocks
    AccountServiceImpl accountService;

    Long userId = 1L;

    // ==================== getBalance ====================

    @Nested
    @DisplayName("getBalance — 查询余额")
    class GetBalanceTests {

        @Test
        @DisplayName("should_return_balance_when_user_exists")
        void shouldReturnBalance_whenUserExists() {
            FmUser user = TestDataFactory.createBuyer(userId, "buyer1");
            user.setBalance(BigDecimal.valueOf(500.00));
            when(fmUserMapper.selectById(userId)).thenReturn(user);

            BigDecimal balance = accountService.getBalance(userId);

            assertThat(balance).isEqualByComparingTo(BigDecimal.valueOf(500.00));
        }

        @Test
        @DisplayName("should_return_zero_when_balance_is_null")
        void shouldReturnZero_whenBalanceIsNull() {
            FmUser user = TestDataFactory.createBuyer(userId, "buyer1");
            user.setBalance(null);
            when(fmUserMapper.selectById(userId)).thenReturn(user);

            BigDecimal balance = accountService.getBalance(userId);

            assertThat(balance).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should_throw_user_not_found_when_get_balance_with_invalid_id")
        void shouldThrowUserNotFound_whenGetBalanceWithInvalidId() {
            when(fmUserMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> accountService.getBalance(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.USER_NOT_FOUND.getCode());
        }
    }

    // ==================== recharge ====================

    @Nested
    @DisplayName("recharge — 账户充值")
    class RechargeTests {

        @Test
        @DisplayName("should_recharge_successfully_when_valid_amount")
        void shouldRechargeSuccessfully_whenValidAmount() {
            FmUser user = TestDataFactory.createBuyer(userId, "buyer1");
            user.setBalance(BigDecimal.valueOf(100.00));
            BigDecimal rechargeAmount = BigDecimal.valueOf(50.00);
            ArgumentCaptor<FmBalanceLog> logCaptor = ArgumentCaptor.forClass(FmBalanceLog.class);

            when(fmUserMapper.selectById(userId)).thenReturn(user);
            when(fmUserMapper.update(isNull(), any(Wrapper.class))).thenReturn(1);
            doReturn(1).when(fmBalanceLogMapper).insert(logCaptor.capture());

            accountService.recharge(userId, rechargeAmount);

            verify(fmUserMapper).update(isNull(), any(Wrapper.class));
            FmBalanceLog capturedLog = logCaptor.getValue();
            assertThat(capturedLog.getAmount()).isEqualByComparingTo(rechargeAmount);
            assertThat(capturedLog.getType()).isEqualTo(BalanceLogTypeEnum.RECHARGE.name());
        }

        @Test
        @DisplayName("should_record_correct_before_and_after_balance_when_recharge")
        void shouldRecordCorrectBeforeAndAfterBalance_whenRecharge() {
            FmUser user = TestDataFactory.createBuyer(userId, "buyer1");
            user.setBalance(BigDecimal.valueOf(100.00));
            BigDecimal rechargeAmount = BigDecimal.valueOf(50.00);
            ArgumentCaptor<FmBalanceLog> logCaptor = ArgumentCaptor.forClass(FmBalanceLog.class);

            when(fmUserMapper.selectById(userId)).thenReturn(user);
            when(fmUserMapper.update(isNull(), any(Wrapper.class))).thenReturn(1);
            doReturn(1).when(fmBalanceLogMapper).insert(logCaptor.capture());

            accountService.recharge(userId, rechargeAmount);

            FmBalanceLog capturedLog = logCaptor.getValue();
            assertThat(capturedLog.getBeforeBalance()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
            assertThat(capturedLog.getAfterBalance()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
        }

        @Test
        @DisplayName("should_handle_recharge_when_before_balance_is_null")
        void shouldHandleRecharge_whenBeforeBalanceIsNull() {
            FmUser user = TestDataFactory.createBuyer(userId, "buyer1");
            user.setBalance(null);
            BigDecimal rechargeAmount = BigDecimal.valueOf(50.00);
            ArgumentCaptor<FmBalanceLog> logCaptor = ArgumentCaptor.forClass(FmBalanceLog.class);

            when(fmUserMapper.selectById(userId)).thenReturn(user);
            when(fmUserMapper.update(isNull(), any(Wrapper.class))).thenReturn(1);
            doReturn(1).when(fmBalanceLogMapper).insert(logCaptor.capture());

            accountService.recharge(userId, rechargeAmount);

            FmBalanceLog capturedLog = logCaptor.getValue();
            assertThat(capturedLog.getBeforeBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(capturedLog.getAfterBalance()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        }

        @Test
        @DisplayName("should_throw_user_not_found_when_recharge_with_invalid_id")
        void shouldThrowUserNotFound_whenRechargeWithInvalidId() {
            when(fmUserMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> accountService.recharge(999L, BigDecimal.TEN))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.USER_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("should_throw_recharge_failed_when_update_rows_is_zero")
        void shouldThrowRechargeFailed_whenUpdateRowsIsZero() {
            FmUser user = TestDataFactory.createBuyer(userId, "buyer1");
            user.setBalance(BigDecimal.valueOf(100.00));

            when(fmUserMapper.selectById(userId)).thenReturn(user);
            when(fmUserMapper.update(isNull(), any(Wrapper.class))).thenReturn(0);

            assertThatThrownBy(() -> accountService.recharge(userId, BigDecimal.TEN))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.RECHARGE_FAILED.getCode());
        }
    }

    // ==================== getBalanceLogs ====================

    @Nested
    @DisplayName("getBalanceLogs — 查询余额流水")
    class GetBalanceLogsTests {

        @Test
        @DisplayName("should_return_paginated_logs_when_user_has_logs")
        void shouldReturnPaginatedLogs_whenUserHasLogs() {
            FmBalanceLog log = new FmBalanceLog();
            log.setId(1L);
            log.setUserId(userId);
            log.setAmount(BigDecimal.valueOf(100));
            log.setBeforeBalance(BigDecimal.valueOf(400));
            log.setAfterBalance(BigDecimal.valueOf(500));
            log.setType(BalanceLogTypeEnum.RECHARGE.name());

            Page<FmBalanceLog> mpPage = new Page<>(1, 20);
            mpPage.setRecords(List.of(log));
            mpPage.setTotal(1);

            when(fmBalanceLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mpPage);

            PageResult<BalanceLogVO> result = accountService.getBalanceLogs(userId, 1, 20);

            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getRecords()).hasSize(1);
            assertThat(result.getRecords().get(0).getType()).isEqualTo(BalanceLogTypeEnum.RECHARGE.name());
        }

        @Test
        @DisplayName("should_return_empty_page_when_no_logs")
        void shouldReturnEmptyPage_whenNoLogs() {
            Page<FmBalanceLog> mpPage = new Page<>(1, 20);
            mpPage.setRecords(Collections.emptyList());
            mpPage.setTotal(0);

            when(fmBalanceLogMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mpPage);

            PageResult<BalanceLogVO> result = accountService.getBalanceLogs(userId, 1, 20);

            assertThat(result.getTotal()).isEqualTo(0);
            assertThat(result.getRecords()).isEmpty();
        }
    }
}
