package com.github.foli_backend.constant;

/**
 * 店铺状态枚举 Store status enum
 */
public enum StoreStatusEnum {
    /** 待审核 pending review */
    PENDING(0),
    /** 已通过 approved */
    APPROVED(1),
    /** 已拒绝 rejected */
    REJECTED(2),
    /** 已关闭 closed */
    CLOSED(3);

    private final int code;

    StoreStatusEnum(int code) { this.code = code; }
    public int getCode() { return code; }
}
