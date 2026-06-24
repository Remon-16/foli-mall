package com.github.foli_backend.service;

import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.dto.response.BalanceLogVO;

import java.math.BigDecimal;

/**
 * 账户服务接口 Account service interface
 * 提供余额查询、充值、余额流水查询功能
 * Provides balance query, recharge, and balance log query
 */
public interface AccountService {

    /**
     * 查询用户余额 Query user balance
     * @param userId 用户ID User ID
     * @return 当前余额 Current balance
     */
    BigDecimal getBalance(Long userId);

    /**
     * 用户充值 Recharge user balance
     * 增加用户余额并记录余额流水
     * Adds amount to user balance and records balance log
     * @param userId 用户ID User ID
     * @param amount 充值金额 Recharge amount
     */
    void recharge(Long userId, BigDecimal amount);

    /**
     * 查询余额变动日志 Query balance change logs
     * @param userId 用户ID User ID
     * @param page 页码 Page number (1-based)
     * @param pageSize 每页条数 Page size
     * @return 分页结果 Paginated result
     */
    PageResult<BalanceLogVO> getBalanceLogs(Long userId, int page, int pageSize);
}
