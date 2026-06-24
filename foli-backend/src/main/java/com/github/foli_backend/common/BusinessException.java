package com.github.foli_backend.common;

import com.github.foli_backend.constant.BizCodeEnum;

/**
 * 业务异常 Business exception
 * 携带6位业务状态码，由 GlobalExceptionHandler 统一处理
 */
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(BizCodeEnum bizCode) {
        super(bizCode.getDefaultMessage());
        this.code = bizCode.getCode();
    }

    public BusinessException(BizCodeEnum bizCode, String detail) {
        super(detail);
        this.code = bizCode.getCode();
    }

    /** 兼容旧代码：直接传 code 和 message */
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
