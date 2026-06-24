package com.github.foli_backend.dto.response;

import com.github.foli_backend.entity.FmReturnRefund;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退货退款视图对象 / Return/refund view object
 */
@Schema(description = "退货退款记录 Return/refund record")
public class ReturnRefundVO {

    @Schema(description = "退货ID Return ID")
    private Long id;

    @Schema(description = "退货编号 Return number")
    private String returnNo;

    @Schema(description = "订单ID Order ID")
    private Long orderId;

    @Schema(description = "订单编号 Order number")
    private String orderNo;

    @Schema(description = "买家用户ID Buyer user ID")
    private Long userId;

    @Schema(description = "买家昵称 Buyer nickname")
    private String buyerNickname;

    @Schema(description = "店铺ID Store ID")
    private Long storeId;

    @Schema(description = "店铺名称 Store name")
    private String storeName;

    @Schema(description = "退货原因 Return reason")
    private String returnReason;

    @Schema(description = "退货类型 Return type: 0=仅退款 1=退货退款")
    private Integer returnType;

    @Schema(description = "退款金额 Refund amount")
    private BigDecimal refundAmount;

    @Schema(description = "状态 Status: 0=待审核 1=通过 2=拒绝 3=买家退回中 4=卖家已收货 5=验货中 6=已退款 7=争议中")
    private Integer status;

    @Schema(description = "卖家备注 Seller comment")
    private String sellerComment;

    @Schema(description = "凭证图片 Evidence images")
    private String evidenceImages;

    @Schema(description = "平台处理结果 Admin handle result")
    private String adminHandleResult;

    @Schema(description = "创建时间 Create time")
    private LocalDateTime createTime;

    @Schema(description = "买家退回时间 Ship back time")
    private LocalDateTime shipBackTime;

    @Schema(description = "退款时间 Refund time")
    private LocalDateTime refundTime;

    /**
     * 从实体构建视图对象 Build view object from entity
     *
     * @param entity 退货退款实体 return/refund entity
     * @return ReturnRefundVO
     */
    public static ReturnRefundVO fromEntity(FmReturnRefund entity) {
        ReturnRefundVO vo = new ReturnRefundVO();
        vo.setId(entity.getId());
        vo.setReturnNo(entity.getReturnNo());
        vo.setOrderId(entity.getOrderId());
        vo.setUserId(entity.getUserId());
        vo.setStoreId(entity.getStoreId());
        vo.setReturnReason(entity.getReturnReason());
        vo.setReturnType(entity.getReturnType());
        vo.setRefundAmount(entity.getRefundAmount());
        vo.setStatus(entity.getStatus());
        vo.setSellerComment(entity.getSellerComment());
        vo.setEvidenceImages(entity.getEvidenceImages());
        vo.setAdminHandleResult(entity.getAdminHandleResult());
        vo.setCreateTime(entity.getCreateTime());
        vo.setShipBackTime(entity.getShipBackTime());
        vo.setRefundTime(entity.getRefundTime());
        return vo;
    }

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

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getBuyerNickname() {
        return buyerNickname;
    }

    public void setBuyerNickname(String buyerNickname) {
        this.buyerNickname = buyerNickname;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getShipBackTime() {
        return shipBackTime;
    }

    public void setShipBackTime(LocalDateTime shipBackTime) {
        this.shipBackTime = shipBackTime;
    }

    public LocalDateTime getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(LocalDateTime refundTime) {
        this.refundTime = refundTime;
    }
}
