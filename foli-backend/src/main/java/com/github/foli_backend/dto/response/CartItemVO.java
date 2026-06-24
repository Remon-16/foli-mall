package com.github.foli_backend.dto.response;

import com.github.foli_backend.entity.FmCartItem;
import com.github.foli_backend.entity.FmProduct;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * 购物车项视图对象 / Cart item view object
 */
@Schema(description = "购物车项 Cart item")
public class CartItemVO {

    @Schema(description = "购物车项ID Cart item ID")
    private Long id;

    @Schema(description = "商品ID Product ID")
    private Long productId;

    @Schema(description = "商品名称 Product name")
    private String productName;

    @Schema(description = "商品图片 Product image")
    private String productImage;

    @Schema(description = "商品单价 Unit price")
    private BigDecimal price;

    @Schema(description = "商品库存 Stock")
    private Integer stock;

    @Schema(description = "购买数量 Quantity")
    private Integer quantity;

    @Schema(description = "是否选中 Selected (1=选中 0=未选中)")
    private Integer selected;

    /**
     * 从实体构建视图对象 Build view object from entities
     *
     * @param item    购物车项 cart item entity
     * @param product 商品 product entity
     * @return CartItemVO
     */
    public static CartItemVO fromEntity(FmCartItem item, FmProduct product) {
        CartItemVO vo = new CartItemVO();
        vo.setId(item.getId());
        vo.setProductId(item.getProductId());
        vo.setProductName(product != null ? product.getName() : null);
        vo.setProductImage(product != null ? product.getMainImage() : null);
        vo.setPrice(product != null ? product.getPrice() : null);
        vo.setStock(product != null ? product.getStock() : null);
        vo.setQuantity(item.getQuantity());
        vo.setSelected(item.getSelected());
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

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

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
