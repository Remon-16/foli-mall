package com.github.foli_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.constant.BalanceLogTypeEnum;
import com.github.foli_backend.dto.response.BalanceLogVO;
import com.github.foli_backend.entity.FmBalanceLog;
import com.github.foli_backend.entity.FmUser;
import com.github.foli_backend.mapper.FmBalanceLogMapper;
import com.github.foli_backend.mapper.FmUserMapper;
import com.github.foli_backend.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账户服务实现 Account service implementation
 * 处理用户余额查询、充值、余额流水记录
 * Handles user balance query, recharge, and balance log recording
 */
@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final FmUserMapper fmUserMapper;
    private final FmBalanceLogMapper fmBalanceLogMapper;

    /**
     * 构造器注入 Constructor injection
     */
    public AccountServiceImpl(FmUserMapper fmUserMapper, FmBalanceLogMapper fmBalanceLogMapper) {
        this.fmUserMapper = fmUserMapper;
        this.fmBalanceLogMapper = fmBalanceLogMapper;
    }

    @Override
    public BigDecimal getBalance(Long userId) {
        FmUser user = fmUserMapper.selectById(userId);
        if (user == null) {
            BizCodeEnum.USER_NOT_FOUND.throwEx();
        }
        return user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recharge(Long userId, BigDecimal amount) {
        FmUser user = fmUserMapper.selectById(userId);
        if (user == null) {
            BizCodeEnum.USER_NOT_FOUND.throwEx();
        }

        BigDecimal beforeBalance = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
        BigDecimal afterBalance = beforeBalance.add(amount);

        // 使用 UpdateWrapper 原子增加余额
        // Atomically increase balance using UpdateWrapper
        UpdateWrapper<FmUser> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("balance = balance + {0}", amount)
                .eq("id", userId);
        int rows = fmUserMapper.update(null, updateWrapper);
        if (rows == 0) {
            BizCodeEnum.RECHARGE_FAILED.throwEx();
        }

        // 记录余额变动日志 Record balance change log
        FmBalanceLog balanceLog = new FmBalanceLog();
        balanceLog.setUserId(userId);
        balanceLog.setAmount(amount);
        balanceLog.setType(BalanceLogTypeEnum.RECHARGE.name());
        balanceLog.setBeforeBalance(beforeBalance);
        balanceLog.setAfterBalance(afterBalance);
        balanceLog.setRemark("账户充值 Account recharge");
        balanceLog.setCreateTime(LocalDateTime.now());

        fmBalanceLogMapper.insert(balanceLog);

        log.info("User {} recharged {}, balance: {} -> {}", userId, amount, beforeBalance, afterBalance);
    }

    @Override
    public PageResult<BalanceLogVO> getBalanceLogs(Long userId, int page, int pageSize) {
        // 构建分页查询 Build paginated query
        Page<FmBalanceLog> mpPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<FmBalanceLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FmBalanceLog::getUserId, userId)
                .orderByDesc(FmBalanceLog::getCreateTime);

        Page<FmBalanceLog> result = fmBalanceLogMapper.selectPage(mpPage, wrapper);

        // 转换为VO Convert to VO
        List<BalanceLogVO> voList = result.getRecords().stream()
                .map(BalanceLogVO::fromEntity)
                .collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), (int) result.getCurrent(), (int) result.getSize(), voList);
    }
}
