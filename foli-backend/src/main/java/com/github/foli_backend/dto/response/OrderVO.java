package com.github.foli_backend.dto.response;

import com.github.foli_backend.entity.FmOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单视图对象 / Order view object
 */
@Schema(description = "订单 Order")
public class OrderVO {

    @Schema(description = "订单ID Order ID")
    private Long id;

    @Schema(description = "订单编号 Order number")
    private String orderNo;

    @Schema(description = "店铺ID Store ID")
    private Long storeId;

    @Schema(description = "店铺名称 Store name")
    private String storeName;

    @Schema(description = "订单总金额 Total amount")
    private BigDecimal totalAmount;

    @Schema(description = "订单状态 Order status: 0=待支付 1=已支付 2=已发货 3=已收货 4=已完成 5=已取消")
    private Integer status;

    @Schema(description = "收货人姓名 Receiver name")
    private String receiverName;

    @Schema(description = "收货人手机号 Receiver phone")
    private String receiverPhone;

    @Schema(description = "收货地址 Receiver address")
    private String receiverAddress;

    @Schema(description = "创建时间 Create time")
    private LocalDateTime createTime;

    @Schema(description = "支付时间 Pay time")
    private LocalDateTime payTime;

    @Schema(description = "发货时间 Ship time")
    private LocalDateTime shipTime;

    @Schema(description = "收货时间 Receive time")
    private LocalDateTime receiveTime;

    @Schema(description = "订单项列表 Order items")
    private List<OrderItemVO> items;

    /**
     * 从实体构建视图对象 Build view object from entity
     *
     * @param order 订单 order entity
     * @return OrderVO
     */
    public static OrderVO fromEntity(FmOrder order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setStoreId(order.getStoreId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setCreateTime(order.getCreateTime());
        vo.setPayTime(order.getPayTime());
        vo.setShipTime(order.getShipTime());
        vo.setReceiveTime(order.getReceiveTime());
        return vo;
    }

    // ========== Getters and Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getPayTime() {
        return payTime;
    }

    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }

    public LocalDateTime getShipTime() {
        return shipTime;
    }

    public void setShipTime(LocalDateTime shipTime) {
        this.shipTime = shipTime;
    }

    public LocalDateTime getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(LocalDateTime receiveTime) {
        this.receiveTime = receiveTime;
    }

    public List<OrderItemVO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemVO> items) {
        this.items = items;
    }
}
