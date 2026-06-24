package com.github.foli_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 充值请求 Recharge request DTO
 */
@Schema(description = "充值请求 Recharge request")
public class RechargeRequest {

    @Schema(description = "充值金额 Recharge amount", example = "100.00")
    @NotNull(message = "充值金额不能为空 Amount cannot be null")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0.01 Amount must be greater than 0.01")
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
