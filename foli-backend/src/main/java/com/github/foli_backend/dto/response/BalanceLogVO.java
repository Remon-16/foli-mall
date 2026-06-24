package com.github.foli_backend.dto.response;

import com.github.foli_backend.entity.FmBalanceLog;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 余额日志视图对象 Balance log view object
 */
@Schema(description = "余额日志 Balance log")
public class BalanceLogVO {

    @Schema(description = "日志ID Log ID")
    private Long id;

    @Schema(description = "变动金额 Change amount")
    private BigDecimal amount;

    @Schema(description = "变动类型 Change type: RECHARGE/PAY/REFUND")
    private String type;

    @Schema(description = "关联订单号 Related order number")
    private String orderNo;

    @Schema(description = "变动前余额 Balance before change")
    private BigDecimal beforeBalance;

    @Schema(description = "变动后余额 Balance after change")
    private BigDecimal afterBalance;

    @Schema(description = "备注 Remark")
    private String remark;

    @Schema(description = "创建时间 Create time")
    private LocalDateTime createTime;

    /**
     * 从实体转换 Convert from entity
     * @param log 余额日志实体 Balance log entity
     * @return 视图对象 View object
     */
    public static BalanceLogVO fromEntity(FmBalanceLog log) {
        BalanceLogVO vo = new BalanceLogVO();
        vo.setId(log.getId());
        vo.setAmount(log.getAmount());
        vo.setType(log.getType());
        vo.setOrderNo(log.getOrderNo());
        vo.setBeforeBalance(log.getBeforeBalance());
        vo.setAfterBalance(log.getAfterBalance());
        vo.setRemark(log.getRemark());
        vo.setCreateTime(log.getCreateTime());
        return vo;
    }

    // ========== Getters and Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public BigDecimal getBeforeBalance() {
        return beforeBalance;
    }

    public void setBeforeBalance(BigDecimal beforeBalance) {
        this.beforeBalance = beforeBalance;
    }

    public BigDecimal getAfterBalance() {
        return afterBalance;
    }

    public void setAfterBalance(BigDecimal afterBalance) {
        this.afterBalance = afterBalance;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
