package com.github.foli_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 退货争议请求 / Return dispute request (seller)
 */
@Schema(description = "退货争议请求 Return dispute request")
public class ReturnDisputeRequest {

    @Schema(description = "卖家争议说明/举证 Seller dispute description")
    private String sellerComment;

    // ========== Getters and Setters ==========

    public String getSellerComment() {
        return sellerComment;
    }

    public void setSellerComment(String sellerComment) {
        this.sellerComment = sellerComment;
    }
}
