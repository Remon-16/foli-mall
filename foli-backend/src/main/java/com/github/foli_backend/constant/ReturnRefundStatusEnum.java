package com.github.foli_backend.constant;

/**
 * 退货退款状态枚举 Return / Refund status enum
 */
public enum ReturnRefundStatusEnum {
    /** 待卖家审核 pending seller review */
    PENDING_REVIEW(0),
    /** 审核通过 approved */
    APPROVED(1),
    /** 审核拒绝 rejected */
    REJECTED(2),
    /** 买家退回中 buyer shipping back */
    BUYER_SHIPPING(3),
    /** 卖家已收货 seller confirmed receipt */
    SELLER_RECEIVED(4),
    /** 验货中 inspection in progress */
    INSPECTING(5),
    /** 已退款 refunded */
    REFUNDED(6),
    /** 争议中 disputed */
    DISPUTED(7);

    private final int code;

    ReturnRefundStatusEnum(int code) { this.code = code; }
    public int getCode() { return code; }
}
