package com.github.foli_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 店铺审核请求 DTO
 * Store review request DTO
 * 管理员审核店铺时使用
 * Used by admin when reviewing a store
 */
@Schema(description = "店铺审核请求 Store review request")
public class StoreReviewRequest {

    /**
     * 审核状态 review status
     * 1 = 通过 APPROVED, 2 = 拒绝 REJECTED
     */
    @NotNull(message = "审核状态不能为空 Review status cannot be null")
    @Schema(description = "审核状态: 1=通过, 2=拒绝 Status: 1=APPROVED, 2=REJECTED", example = "1")
    private Integer status;

    /** 审核意见 review comment */
    @Schema(description = "审核意见 Review comment", example = "店铺信息完善，审核通过")
    private String reviewComment;

    // ========== Getters and Setters ==========

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
}
