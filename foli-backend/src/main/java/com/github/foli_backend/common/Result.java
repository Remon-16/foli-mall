package com.github.foli_backend.common;

import com.github.foli_backend.constant.BizCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 统一API响应包装 Unified API response wrapper
 * @param <T> 数据类型 data type
 */
@Schema(description = "统一响应结果 Unified response")
public class Result<T> {

    @Schema(description = "6位业务状态码 Business status code (ABBCCC)")
    private String code;

    @Schema(description = "提示信息 Message")
    private String message;

    @Schema(description = "响应数据 Response data")
    private T data;

    private Result() {}

    private Result(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功 success */
    public static <T> Result<T> success(T data) {
        return new Result<>(BizCodeEnum.SUCCESS.getCode(), "success", data);
    }

    /** 成功 无数据 success without data */
    public static <T> Result<T> success() {
        return new Result<>(BizCodeEnum.SUCCESS.getCode(), "success", null);
    }

    /** 成功 自定义消息 success with custom message */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(BizCodeEnum.SUCCESS.getCode(), message, data);
    }

    /** 失败 使用BizCodeEnum */
    public static <T> Result<T> fail(BizCodeEnum bizCode) {
        return new Result<>(bizCode.getCode(), bizCode.getDefaultMessage(), null);
    }

    /** 失败 使用BizCodeEnum + 自定义消息 */
    public static <T> Result<T> fail(BizCodeEnum bizCode, String detail) {
        return new Result<>(bizCode.getCode(), detail, null);
    }

    /** 失败 直接传code和message */
    public static <T> Result<T> fail(String code, String message) {
        return new Result<>(code, message, null);
    }

    // getters & setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
