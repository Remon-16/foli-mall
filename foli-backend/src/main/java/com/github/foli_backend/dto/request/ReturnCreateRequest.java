package com.github.foli_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 创建退货退款请求 / Create return/refund request
 */
@Schema(description = "创建退货退款请求 Create return/refund request")
public class ReturnCreateRequest {

    @Schema(description = "订单ID Order ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订单ID不能为空 Order ID cannot be null")
    private Long orderId;

    @Schema(description = "退货原因 Return reason")
    private String returnReason;

    @Schema(description = "退货类型 Return type: 0=仅退款 1=退货退款", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "退货类型不能为空 Return type cannot be null")
    private Integer returnType;

    @Schema(description = "凭证图片 Evidence images (comma-separated URLs)")
    private String evidenceImages;

    // ========== Getters and Setters ==========

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public Integer getReturnType() {
        return returnType;
    }

    public void setReturnType(Integer returnType) {
        this.returnType = returnType;
    }

    public String getEvidenceImages() {
        return evidenceImages;
    }

    public void setEvidenceImages(String evidenceImages) {
        this.evidenceImages = evidenceImages;
    }
}
