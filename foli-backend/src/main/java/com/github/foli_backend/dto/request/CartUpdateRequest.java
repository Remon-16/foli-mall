package com.github.foli_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 更新购物车请求 / Update cart item request
 */
@Schema(description = "更新购物车请求 Update cart item request")
public class CartUpdateRequest {

    @Schema(description = "购买数量 Quantity")
    private Integer quantity;

    @Schema(description = "是否选中 Selected (1=选中 0=未选中)")
    private Integer selected;

    // ========== Getters and Setters ==========

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getSelected() {
        return selected;
    }

    public void setSelected(Integer selected) {
        this.selected = selected;
    }
}
