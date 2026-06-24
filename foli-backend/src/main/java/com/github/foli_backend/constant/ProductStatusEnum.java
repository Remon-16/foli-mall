package com.github.foli_backend.constant;

/**
 * 商品状态枚举 Product status enum
 */
public enum ProductStatusEnum {
    /** 草稿 draft */
    DRAFT(0),
    /** 待审核 pending review */
    PENDING_REVIEW(1),
    /** 已审核通过 approved */
    APPROVED(2),
    /** 已拒绝 rejected */
    REJECTED(3),
    /** 已下架 off shelf */
    OFF_SHELF(4);

    private final int code;

    ProductStatusEnum(int code) { this.code = code; }
    public int getCode() { return code; }
}
