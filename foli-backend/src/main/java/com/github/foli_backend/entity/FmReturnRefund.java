package com.github.foli_backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退货退款实体 / Return & refund entity
 */
@TableName("fm_return_refund")
public class FmReturnRefund implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String returnNo;

    private Long orderId;

    private Long userId;

    private Long storeId;

    private String returnReason;

    private Integer returnType;

    private BigDecimal refundAmount;

    private Integer status;

    private String sellerComment;

    private String evidenceImages;

    private String adminHandleResult;

    private LocalDateTime shipBackTime;

    private LocalDateTime sellerReceiveTime;

    private LocalDateTime inspectTime;

    private LocalDateTime refundTime;

    private LocalDateTime disputeTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime editTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDelete;

    // ========== Getters and Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReturnNo() {
        return returnNo;
    }

    public void setReturnNo(String returnNo) {
        this.returnNo = returnNo;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
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

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

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

    public String getEvidenceImages() {
        return evidenceImages;
    }

    public void setEvidenceImages(String evidenceImages) {
        this.evidenceImages = evidenceImages;
    }

    public String getAdminHandleResult() {
        return adminHandleResult;
    }

    public void setAdminHandleResult(String adminHandleResult) {
        this.adminHandleResult = adminHandleResult;
    }

    public LocalDateTime getShipBackTime() {
        return shipBackTime;
    }

    public void setShipBackTime(LocalDateTime shipBackTime) {
        this.shipBackTime = shipBackTime;
    }

    public LocalDateTime getSellerReceiveTime() {
        return sellerReceiveTime;
    }

    public void setSellerReceiveTime(LocalDateTime sellerReceiveTime) {
        this.sellerReceiveTime = sellerReceiveTime;
    }

    public LocalDateTime getInspectTime() {
        return inspectTime;
    }

    public void setInspectTime(LocalDateTime inspectTime) {
        this.inspectTime = inspectTime;
    }

    public LocalDateTime getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(LocalDateTime refundTime) {
        this.refundTime = refundTime;
    }

    public LocalDateTime getDisputeTime() {
        return disputeTime;
    }

    public void setDisputeTime(LocalDateTime disputeTime) {
        this.disputeTime = disputeTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getEditTime() {
        return editTime;
    }

    public void setEditTime(LocalDateTime editTime) {
        this.editTime = editTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }
}
