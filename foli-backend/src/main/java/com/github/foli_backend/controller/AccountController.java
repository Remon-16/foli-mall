package com.github.foli_backend.controller;

import com.github.foli_backend.annotation.RequireLogin;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.common.Result;
import com.github.foli_backend.context.UserContext;
import com.github.foli_backend.dto.request.RechargeRequest;
import com.github.foli_backend.dto.response.BalanceLogVO;
import com.github.foli_backend.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 账户管理控制器 Account management controller
 * 提供余额查询、充值、余额流水接口
 * Provides balance query, recharge, and balance log endpoints
 */
@RestController
@RequestMapping("/api/account")
@Tag(name = "账户管理 Account")
public class AccountController {

    private final AccountService accountService;

    /**
     * 构造器注入 Constructor injection
     */
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * 查询当前用户余额 Query current user's balance
     */
    @GetMapping("/balance")
    @RequireLogin
    @Operation(summary = "查询余额 Query balance", description = "查询当前登录用户的账户余额 Query the current logged-in user's account balance")
    public Result<BigDecimal> getBalance() {
        Long userId = UserContext.getUserId();
        BigDecimal balance = accountService.getBalance(userId);
        return Result.success(balance);
    }

    /**
     * 用户充值 Recharge user balance
     * @param request 充值请求 Recharge request
     */
    @PostMapping("/recharge")
    @RequireLogin
    @Operation(summary = "账户充值 Recharge account", description = "为当前登录用户账户充值 Recharge the current logged-in user's account")
    public Result<Void> recharge(@Valid @RequestBody RechargeRequest request) {
        Long userId = UserContext.getUserId();
        accountService.recharge(userId, request.getAmount());
        return Result.success();
    }

    /**
     * 查询余额变动日志 Query balance change logs
     * @param page 页码 Page number (default 1)
     * @param pageSize 每页条数 Page size (default 20)
     */
    @GetMapping("/balance-logs")
    @RequireLogin
    @Operation(summary = "查询余额流水 Query balance logs", description = "分页查询当前用户的余额变动记录 Paginated query of the current user's balance change records")
    public Result<PageResult<BalanceLogVO>> getBalanceLogs(
            @Parameter(description = "页码 Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数 Page size") @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = UserContext.getUserId();
        PageResult<BalanceLogVO> result = accountService.getBalanceLogs(userId, page, pageSize);
        return Result.success(result);
    }
}
