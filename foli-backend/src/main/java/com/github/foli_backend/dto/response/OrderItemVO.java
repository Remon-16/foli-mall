package com.github.foli_backend.dto.response;

import com.github.foli_backend.entity.FmOrderItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * 订单项视图对象 / Order item view object
 */
@Schema(description = "订单项 Order item")
public class OrderItemVO {

    @Schema(description = "订单项ID Order item ID")
    private Long id;

    @Schema(description = "商品ID Product ID")
    private Long productId;

    @Schema(description = "商品名称 Product name")
    private String productName;

    @Schema(description = "商品图片 Product image")
    private String productImage;

    @Schema(description = "商品单价 Unit price")
    private BigDecimal price;

    @Schema(description = "购买数量 Quantity")
    private Integer quantity;

    /**
     * 从实体构建视图对象 Build view object from entity
     *
     * @param item 订单项 order item entity
     * @return OrderItemVO
     */
    public static OrderItemVO fromEntity(FmOrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        vo.setId(item.getId());
        vo.setProductId(item.getProductId());
        vo.setProductName(item.getProductName());
        vo.setProductImage(item.getProductImage());
        vo.setPrice(item.getPrice());
        vo.setQuantity(item.getQuantity());
        return vo;
    }

    // ========== Getters and Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
