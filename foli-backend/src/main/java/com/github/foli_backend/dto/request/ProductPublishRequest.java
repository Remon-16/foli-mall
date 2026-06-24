package com.github.foli_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品发布/编辑请求 DTO Product publish/edit request DTO
 */
@Schema(description = "商品发布/编辑请求 Product publish/edit request")
public class ProductPublishRequest {

    @Schema(description = "商品名称 Product name", example = "夏季新款T恤")
    @NotBlank(message = "商品名称不能为空 Product name is required")
    private String name;

    @Schema(description = "分类ID Category ID", example = "1")
    @NotNull(message = "商品分类不能为空 Product category is required")
    private Long categoryId;

    @Schema(description = "商品价格 Product price", example = "99.00")
    @NotNull(message = "商品价格不能为空 Product price is required")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0.01 Price must be greater than 0.01")
    private BigDecimal price;

    @Schema(description = "库存数量 Stock quantity", example = "100")
    private Integer stock;

    @Schema(description = "商品描述 Product description", example = "舒适透气的夏季新款T恤")
    private String description;

    @Schema(description = "商品主图 Product main image URL", example = "https://cdn.example.com/product/main.jpg")
    private String mainImage;

    @Schema(description = "商品图片列表 Product image URL list", example = "[\"https://cdn.example.com/product/1.jpg\", \"https://cdn.example.com/product/2.jpg\"]")
    private List<String> images;

    // ========== Getters and Setters ==========

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
