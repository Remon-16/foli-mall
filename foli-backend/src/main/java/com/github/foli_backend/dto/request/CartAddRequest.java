package com.github.foli_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 添加购物车请求 / Add to cart request
 */
@Schema(description = "添加购物车请求 Add to cart request")
public class CartAddRequest {

    @Schema(description = "商品ID Product ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品ID不能为空 Product ID cannot be null")
    private Long productId;

    @Schema(description = "购买数量 Quantity", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer quantity = 1;

    // ========== Getters and Setters ==========

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
