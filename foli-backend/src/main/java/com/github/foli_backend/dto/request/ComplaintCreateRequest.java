package com.github.foli_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 创建投诉请求 / Create complaint request
 */
@Schema(description = "创建投诉请求 Create complaint request")
public class ComplaintCreateRequest {

    @Schema(description = "被投诉店铺ID Store ID (optional, required if reportedUserId is null)")
    private Long storeId;

    @Schema(description = "被投诉用户ID Reported user ID (for seller complaints against buyers)")
    private Long reportedUserId;

    @Schema(description = "关联订单ID Related order ID (optional)")
    private Long orderId;

    @Schema(description = "关联商品ID Related product ID (optional)")
    private Long productId;

    @Schema(description = "关联退货ID Related return ID (optional)")
    private Long returnId;

    @Schema(description = "投诉类型 Complaint type")
    private String type;

    @Schema(description = "投诉标题 Title", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "投诉标题不能为空 Title cannot be blank")
    private String title;

    @Schema(description = "投诉内容 Content", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "投诉内容不能为空 Content cannot be blank")
    private String content;

    @Schema(description = "凭证图片 Evidence images (comma-separated URLs)")
    private String evidenceImages;

    // ========== Getters and Setters ==========

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(Long reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getReturnId() {
        return returnId;
    }

    public void setReturnId(Long returnId) {
        this.returnId = returnId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEvidenceImages() {
        return evidenceImages;
    }

    public void setEvidenceImages(String evidenceImages) {
        this.evidenceImages = evidenceImages;
    }
}
