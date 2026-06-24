package com.github.foli_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * 商品搜索请求 DTO Product search request DTO
 */
@Schema(description = "商品搜索请求 Product search request")
public class ProductSearchRequest {

    @Schema(description = "搜索关键词 Search keyword", example = "T恤")
    private String keyword;

    @Schema(description = "分类ID Category ID", example = "1")
    private Long categoryId;

    @Schema(description = "最低价格 Minimum price", example = "10.00")
    private BigDecimal minPrice;

    @Schema(description = "最高价格 Maximum price", example = "200.00")
    private BigDecimal maxPrice;

    @Schema(description = "排序方式: default(默认)/newest(最新)/price_asc(价格升序)/price_desc(价格降序)/sales(销量) Sort by", example = "newest")
    private String sortBy;

    @Schema(description = "当前页码 Page number", example = "1")
    private Integer page = 1;

    @Schema(description = "每页大小 Page size", example = "20")
    private Integer pageSize = 20;

    // ========== Getters and Setters ==========

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
