package com.github.foli_backend.dto.response;

import com.github.foli_backend.entity.FmComplaint;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 投诉视图对象 / Complaint view object
 */
@Schema(description = "投诉 Complaint")
public class ComplaintVO {

    @Schema(description = "投诉ID Complaint ID")
    private Long id;

    @Schema(description = "投诉人用户ID Complainant user ID")
    private Long userId;

    @Schema(description = "投诉人名称 Complainant name")
    private String userName;

    @Schema(description = "关联订单ID Related order ID")
    private Long orderId;

    @Schema(description = "关联商品ID Related product ID")
    private Long productId;

    @Schema(description = "被投诉店铺ID Target store ID")
    private Long storeId;

    @Schema(description = "店铺名称 Store name")
    private String storeName;

    @Schema(description = "关联退货ID Related return ID")
    private Long returnId;

    @Schema(description = "投诉类型 Complaint type")
    private String type;

    @Schema(description = "投诉标题 Title")
    private String title;

    @Schema(description = "投诉内容 Content")
    private String content;

    @Schema(description = "凭证图片 Evidence images")
    private String evidenceImages;

    @Schema(description = "状态 Status: 0=待处理 1=处理中 2=已解决 3=已驳回")
    private Integer status;

    @Schema(description = "处理人ID Handler ID")
    private Long handlerId;

    @Schema(description = "处理结果 Handle result")
    private String handleResult;

    @Schema(description = "处理时间 Handle time")
    private LocalDateTime handleTime;

    @Schema(description = "创建时间 Create time")
    private LocalDateTime createTime;

    /**
     * 从实体构建视图对象 Build view object from entity
     *
     * @param c 投诉实体 complaint entity
     * @return ComplaintVO
     */
    public static ComplaintVO fromEntity(FmComplaint c) {
        ComplaintVO vo = new ComplaintVO();
        vo.setId(c.getId());
        vo.setUserId(c.getUserId());
        vo.setOrderId(c.getOrderId());
        vo.setProductId(c.getProductId());
        vo.setStoreId(c.getStoreId());
        vo.setReturnId(c.getReturnId());
        vo.setType(c.getType());
        vo.setTitle(c.getTitle());
        vo.setContent(c.getContent());
        vo.setEvidenceImages(c.getEvidenceImages());
        vo.setStatus(c.getStatus());
        vo.setHandlerId(c.getHandlerId());
        vo.setHandleResult(c.getHandleResult());
        vo.setHandleTime(c.getHandleTime());
        vo.setCreateTime(c.getCreateTime());
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getHandlerId() {
        return handlerId;
    }

    public void setHandlerId(Long handlerId) {
        this.handlerId = handlerId;
    }

    public String getHandleResult() {
        return handleResult;
    }

    public void setHandleResult(String handleResult) {
        this.handleResult = handleResult;
    }

    public LocalDateTime getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(LocalDateTime handleTime) {
        this.handleTime = handleTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
