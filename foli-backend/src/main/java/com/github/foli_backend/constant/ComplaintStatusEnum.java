package com.github.foli_backend.constant;

/**
 * 投诉状态枚举 Complaint status enum
 */
public enum ComplaintStatusEnum {
    /** 待处理 pending */
    PENDING(0),
    /** 处理中 in progress */
    IN_PROGRESS(1),
    /** 已解决 resolved */
    RESOLVED(2),
    /** 已驳回 rejected */
    REJECTED(3);

    private final int code;

    ComplaintStatusEnum(int code) { this.code = code; }
    public int getCode() { return code; }
}
