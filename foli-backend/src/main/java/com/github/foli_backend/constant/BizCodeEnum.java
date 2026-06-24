package com.github.foli_backend.constant;

import com.github.foli_backend.common.BusinessException;

/**
 * 6位业务状态码枚举
 * 格式: ABBCCC
 *   A: 错误级别 (1=成功, 2=业务异常, 3=系统异常)
 *   BB: 模块编号 (00=通用, 01=用户, 02=店铺, 03=商品, 04=订单, 05=资金, 06=文件, 07=购物车, 08=退货, 09=投诉, 10=消息, 11=分类)
 *   CCC: 模块内错误序号
 */
public enum BizCodeEnum {

    // ==================== 通用 (00) ====================
    SUCCESS             ("100000", "成功"),
    BAD_REQUEST         ("200001", "请求参数错误"),
    NOT_FOUND           ("200002", "请求的资源不存在"),
    FORBIDDEN           ("200003", "无权执行此操作"),
    INTERNAL_ERROR      ("300001", "系统内部错误"),

    // ==================== 用户模块 (01) ====================
    USERNAME_EXISTS     ("201001", "用户名已存在"),
    INVALID_CREDENTIALS ("201002", "用户名或密码错误"),
    ACCOUNT_DISABLED    ("201003", "账号已被禁用"),
    USER_NOT_FOUND      ("201004", "用户不存在"),

    // ==================== 店铺模块 (02) ====================
    STORE_LIMIT_EXCEEDED  ("202001", "您已拥有一个店铺，请勿重复申请"),
    STORE_ALREADY_REVIEWED("202002", "该店铺已审核完毕，不可重复审核"),
    STORE_NOT_FOUND       ("202003", "店铺不存在"),
    INVALID_REVIEW_STATUS ("202004", "无效的审核状态"),
    NOT_SELLER_ROLE       ("202005", "仅卖家可申请开店"),
    STORE_NOT_CREATED     ("202006", "您还没有店铺，请先创建店铺"),

    // ==================== 商品与库存模块 (03) ====================
    PRODUCT_NOT_FOUND       ("203001", "商品不存在"),
    INSUFFICIENT_STOCK      ("203002", "库存不足"),
    STOCK_DEDUCTION_FAILED  ("203003", "扣减库存失败，商品已售罄"),
    PRODUCT_OFF_SHELF       ("203004", "商品已下架"),
    PRODUCT_ALREADY_REVIEWED("203005", "该商品已审核完毕，不可重复审核"),

    // ==================== 订单模块 (04) ====================
    ORDER_NOT_FOUND         ("204001", "订单不存在"),
    WRONG_ORDER_STATUS      ("204002", "订单状态不正确，不允许此操作"),
    ORDER_NOT_COMPLETED     ("204003", "仅已完成订单可申请退货退款"),

    // ==================== 资金模块 (05) ====================
    INSUFFICIENT_BALANCE    ("205001", "余额不足"),
    BALANCE_DEDUCTION_FAILED("205002", "余额扣减失败"),
    RECHARGE_FAILED         ("205003", "充值失败，余额更新异常"),

    // ==================== 文件模块 (06) ====================
    FILE_EMPTY        ("206001", "上传文件不能为空"),
    FILE_TOO_LARGE    ("206002", "文件大小不能超过10MB"),
    FILE_NOT_IMAGE    ("206003", "仅支持上传图片文件"),
    FILE_UPLOAD_FAILED("206004", "文件上传失败"),

    // ==================== 购物车模块 (07) ====================
    CART_EMPTY              ("207001", "没有选中的商品"),
    CART_ITEM_NOT_FOUND     ("207002", "购物车项不存在或无权限操作"),

    // ==================== 退货退款模块 (08) ====================
    RETURN_NOT_FOUND        ("208001", "退货记录不存在"),
    WRONG_RETURN_STATUS     ("208002", "退货状态不正确，不允许此操作"),
    RETURN_ALREADY_PROCESSED("208003", "该退货申请已处理"),

    // ==================== 投诉模块 (09) ====================
    COMPLAINT_NOT_FOUND         ("209001", "投诉不存在"),
    COMPLAINT_ALREADY_HANDLED   ("209002", "该投诉已处理完毕，不可重复处理"),

    // ==================== 消息模块 (10) ====================
    MESSAGE_NOT_FOUND  ("210001", "消息不存在"),

    // ==================== 分类模块 (11) ====================
    CATEGORY_NOT_FOUND    ("211001", "分类不存在"),
    CATEGORY_HAS_CHILDREN ("211002", "该分类下存在子分类，无法删除"),
    CATEGORY_PARENT_SELF  ("211003", "父分类不能设置为自身");

    private final String code;
    private final String defaultMessage;

    BizCodeEnum(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() { return code; }
    public String getDefaultMessage() { return defaultMessage; }

    /** 抛出 BusinessException，使用默认消息 */
    public void throwEx() {
        throw new BusinessException(this.code, this.defaultMessage);
    }

    /** 抛出 BusinessException，附加详情（使用默认消息拼接） */
    public void throwEx(String detail) {
        throw new BusinessException(this.code, this.defaultMessage + " " + detail);
    }
}
