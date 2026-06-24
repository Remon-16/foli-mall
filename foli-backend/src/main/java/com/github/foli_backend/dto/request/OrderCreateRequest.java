package com.github.foli_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 创建订单请求 / Create order request
 */
@Schema(description = "创建订单请求 Create order request")
public class OrderCreateRequest {

    @Schema(description = "收货人姓名 Receiver name")
    private String receiverName;

    @Schema(description = "收货人手机号 Receiver phone")
    private String receiverPhone;

    @Schema(description = "收货地址 Receiver address")
    private String receiverAddress;

    // ========== Getters and Setters ==========

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
}
