package com.github.foli_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 商品审核请求 DTO Product review request DTO
 */
@Schema(description = "商品审核请求 Product review request")
public class ProductReviewRequest {

    @Schema(description = "审核状态: 2=审核通过, 3=审核拒绝 Review status: 2=Approved, 3=Rejected", example = "2")
    @NotNull(message = "审核状态不能为空 Review status is required")
    private Integer status;

    @Schema(description = "审核意见 Review comment", example = "商品信息真实有效，审核通过")
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
