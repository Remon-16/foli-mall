package com.github.foli_backend.dto.response;

import com.github.foli_backend.entity.FmProduct;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品视图对象（列表用） Product view object (for list display)
 */
@Schema(description = "商品视图 Product view")
public class ProductVO {

    @Schema(description = "商品ID Product ID")
    private Long id;

    @Schema(description = "店铺ID Store ID")
    private Long storeId;

    @Schema(description = "店铺名称 Store name")
    private String storeName;

    @Schema(description = "分类ID Category ID")
    private Long categoryId;

    @Schema(description = "分类名称 Category name")
    private String categoryName;

    @Schema(description = "商品名称 Product name")
    private String name;

    @Schema(description = "商品描述 Product description")
    private String description;

    @Schema(description = "商品主图 Main image URL")
    private String mainImage;

    @Schema(description = "商品价格 Product price")
    private BigDecimal price;

    @Schema(description = "库存数量 Stock quantity")
    private Integer stock;

    @Schema(description = "状态: 0=草稿 1=待审核 2=已通过 3=已拒绝 4=已下架 Status")
    private Integer status;

    @Schema(description = "销量 Sales count")
    private Integer salesCount;

    @Schema(description = "商品图片列表 Product image URL list")
    private List<String> images;

    @Schema(description = "创建时间 Create time")
    private LocalDateTime createTime;

    /**
     * 从实体转换为 VO（基础字段，不包含图片列表和关联名称）
     * Convert from entity to VO (basic fields, images=null, storeName/categoryName not populated)
     */
    public static ProductVO fromEntity(FmProduct p) {
        ProductVO vo = new ProductVO();
        vo.setId(p.getId());
        vo.setStoreId(p.getStoreId());
        vo.setCategoryId(p.getCategoryId());
        vo.setName(p.getName());
        vo.setDescription(p.getDescription());
        vo.setMainImage(p.getMainImage());
        vo.setPrice(p.getPrice());
        vo.setStock(p.getStock());
        vo.setStatus(p.getStatus());
        vo.setSalesCount(p.getSalesCount());
        vo.setCreateTime(p.getCreateTime());
        // storeName, categoryName, images 由调用方自行设置
        // storeName, categoryName, images will be set by the caller
        return vo;
    }

    // ========== Getters and Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(Integer salesCount) {
        this.salesCount = salesCount;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
