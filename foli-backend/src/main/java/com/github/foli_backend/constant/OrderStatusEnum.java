package com.github.foli_backend.constant;

/**
 * 订单状态枚举 Order status enum
 */
public enum OrderStatusEnum {
    /** 待支付 pending payment */
    PENDING_PAY(0),
    /** 已支付 paid */
    PAID(1),
    /** 已发货 shipped */
    SHIPPED(2),
    /** 已收货 received */
    RECEIVED(3),
    /** 已完成 completed */
    COMPLETED(4),
    /** 已取消 cancelled */
    CANCELLED(5);

    private final int code;

    OrderStatusEnum(int code) { this.code = code; }
    public int getCode() { return code; }
}
