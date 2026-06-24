package com.github.foli_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 退货审核请求 / Return review request (seller)
 */
@Schema(description = "退货审核请求 Return review request")
public class ReturnReviewRequest {

    @Schema(description = "审核状态 Review status: 1=通过 2=拒绝")
    private Integer status;

    @Schema(description = "卖家备注 Seller comment")
    private String sellerComment;

    // ========== Getters and Setters ==========

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getSellerComment() {
        return sellerComment;
    }

    public void setSellerComment(String sellerComment) {
        this.sellerComment = sellerComment;
    }
}
