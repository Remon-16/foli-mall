package com.github.foli_backend.dto.response;

import com.github.foli_backend.entity.FmStore;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 店铺视图对象 VO (基础信息)
 * Store view object - basic store information returned to frontend
 * 用于列表展示，包含店铺基本统计信息
 * Used for list display, includes basic store stats
 */
@Schema(description = "店铺视图 Store view object")
public class StoreVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺ID Store ID")
    private Long id;

    @Schema(description = "店主用户ID Owner user ID")
    private Long userId;

    @Schema(description = "店铺名称 Store name")
    private String storeName;

    @Schema(description = "店铺Logo Store logo URL")
    private String storeLogo;

    @Schema(description = "店铺简介 Store description")
    private String description;

    @Schema(description = "店铺状态: 0=待审核 1=已通过 2=已拒绝 3=已关闭 Status: 0=PENDING 1=APPROVED 2=REJECTED 3=CLOSED")
    private Integer status;

    @Schema(description = "审核意见 Review comment")
    private String reviewComment;

    @Schema(description = "店主昵称 Owner nickname")
    private String ownerNickname;

    @Schema(description = "商品数量 Product count")
    private Integer productCount;

    @Schema(description = "创建时间 Create time")
    private LocalDateTime createTime;

    /**
     * 从实体对象构建基础 VO (仅填充店铺自身字段)
     * Build basic VO from entity (fills store fields only)
     * ownerNickname 和 productCount 由调用方后续设置
     * ownerNickname and productCount are set by the caller afterwards
     *
     * @param store 店铺实体 store entity
     * @return StoreVO with basic fields populated
     */
    public static StoreVO fromEntity(FmStore store) {
        StoreVO vo = new StoreVO();
        vo.setId(store.getId());
        vo.setUserId(store.getUserId());
        vo.setStoreName(store.getStoreName());
        vo.setStoreLogo(store.getStoreLogo());
        vo.setDescription(store.getDescription());
        vo.setStatus(store.getStatus());
        vo.setReviewComment(store.getReviewComment());
        vo.setCreateTime(store.getCreateTime());
        return vo;
    }

    // ========== Getters and Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreLogo() {
        return storeLogo;
    }

    public void setStoreLogo(String storeLogo) {
        this.storeLogo = storeLogo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public String getOwnerNickname() {
        return ownerNickname;
    }

    public void setOwnerNickname(String ownerNickname) {
        this.ownerNickname = ownerNickname;
    }

    public Integer getProductCount() {
        return productCount;
    }

    public void setProductCount(Integer productCount) {
        this.productCount = productCount;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
