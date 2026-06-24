-- ============================================================
-- Foli Mall 数据库建表 DDL Database Schema
-- 12张表 12 tables
-- 中文注释在前 Chinese comments first, English follows
-- ============================================================

SET MODE MySQL;

-- -----------------------------------------------------------
-- 1. 用户表 System users (buyers, sellers, admins)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS fm_user (
    id           BIGINT        NOT NULL PRIMARY KEY COMMENT '用户ID (雪花) User ID (Snowflake)',
    username     VARCHAR(50)   NOT NULL COMMENT '登录用户名 Login username',
    password     VARCHAR(255)  NOT NULL COMMENT 'BCrypt加密密码 BCrypt-hashed password',
    nickname     VARCHAR(50)   DEFAULT NULL COMMENT '显示昵称 Display nickname',
    phone        VARCHAR(20)   DEFAULT NULL COMMENT '手机号码 Phone number',
    email        VARCHAR(100)  DEFAULT NULL COMMENT '电子邮箱 Email address',
    avatar       VARCHAR(255)  DEFAULT NULL COMMENT '头像图片URL Avatar image URL',
    balance      DECIMAL(10,2) DEFAULT 0.00 NOT NULL COMMENT '账户余额 Account balance',
    role         TINYINT       NOT NULL COMMENT '角色 0=BUYER 1=SELLER 2=ADMIN Role',
    status       TINYINT       DEFAULT 1 COMMENT '状态 0=禁用 1=启用 Status: 0=disabled 1=active',
    create_time   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间 createTime',
    edit_time     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间 editTime',
    update_time   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间 updateTime',
    is_delete     TINYINT       DEFAULT 0 NOT NULL COMMENT '是否删除 0=否 1=是 isDelete',
    CONSTRAINT uk_fm_user_username UNIQUE (username)
);
COMMENT ON TABLE fm_user IS '系统用户表 买家/卖家/管理员 System users (buyers, sellers, admins)';

-- -----------------------------------------------------------
-- 2. 店铺表 Seller stores
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS fm_store (
    id             BIGINT        NOT NULL PRIMARY KEY COMMENT '店铺ID (雪花) Store ID (Snowflake)',
    user_id        BIGINT        NOT NULL COMMENT '店主用户ID (SELLER角色) Owner user ID (seller role)',
    store_name     VARCHAR(100)  NOT NULL COMMENT '店铺名称 Store display name',
    store_logo     VARCHAR(255)  DEFAULT NULL COMMENT '店铺Logo图片URL Store logo image URL',
    description    TEXT          DEFAULT NULL COMMENT '店铺描述 Store description',
    status         TINYINT       DEFAULT 0 COMMENT '状态 0=待审核 1=通过 2=拒绝 3=关闭 Status: 0=pending 1=approved 2=rejected 3=closed',
    review_comment VARCHAR(500)  DEFAULT NULL COMMENT '管理员审核意见 Admin review comment',
    create_time     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间 createTime',
    edit_time       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间 editTime',
    update_time     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间 updateTime',
    is_delete       TINYINT       DEFAULT 0 NOT NULL COMMENT '是否删除 0=否 1=是 isDelete'
);
COMMENT ON TABLE fm_store IS '店铺表 卖家开店需管理员审核 Seller stores subject to admin review';

-- -----------------------------------------------------------
-- 3. 商品分类表 Product categories (tree structure)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS fm_product_category (
    id         BIGINT       NOT NULL PRIMARY KEY COMMENT '分类ID (雪花) Category ID (Snowflake)',
    parent_id  BIGINT       DEFAULT 0 COMMENT '父分类ID 0=顶级 Parent category ID; 0=top level',
    name       VARCHAR(50)  NOT NULL COMMENT '分类名称 Category name',
    icon       VARCHAR(255) DEFAULT NULL COMMENT '分类图标URL Category icon URL',
    sort_order INT          DEFAULT 0 COMMENT '排序号 Display sort order',
    status     TINYINT      DEFAULT 1 COMMENT '状态 0=禁用 1=启用 Status: 0=disabled 1=enabled',
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间 createTime',
    edit_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间 editTime',
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间 updateTime',
    is_delete   TINYINT      DEFAULT 0 NOT NULL COMMENT '是否删除 0=否 1=是 isDelete'
);
COMMENT ON TABLE fm_product_category IS '商品分类表 树形结构 Product category tree';

-- -----------------------------------------------------------
-- 4. 商品表 Products
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS fm_product (
    id             BIGINT         NOT NULL PRIMARY KEY COMMENT '商品ID (雪花) Product ID (Snowflake)',
    store_id       BIGINT         NOT NULL COMMENT '所属店铺ID Belonging store ID',
    category_id    BIGINT         NOT NULL COMMENT '所属分类ID Category ID',
    name           VARCHAR(200)   NOT NULL COMMENT '商品名称 Product name',
    description    TEXT           DEFAULT NULL COMMENT '商品描述 Product description',
    main_image     VARCHAR(500)   DEFAULT NULL COMMENT '主图URL Main product image URL',
    price          DECIMAL(10,2)  NOT NULL COMMENT '单价 Unit price',
    stock          INT            DEFAULT 0 COMMENT '库存数量 Available stock quantity',
    status         TINYINT        DEFAULT 0 COMMENT '状态 0=草稿 1=待审核 2=通过 3=拒绝 4=下架 Status: 0=draft 1=pending 2=approved 3=rejected 4=off-shelf',
    review_comment VARCHAR(500)   DEFAULT NULL COMMENT '审核意见 Review comment',
    sales_count    INT            DEFAULT 0 COMMENT '累计销量 Cumulative sales count',
    create_time     TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间 createTime',
    edit_time       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间 editTime',
    update_time     TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间 updateTime',
    is_delete       TINYINT        DEFAULT 0 NOT NULL COMMENT '是否删除 0=否 1=是 isDelete'
);
COMMENT ON TABLE fm_product IS '商品表 卖家发布需审核 Products published by sellers, subject to admin review';

-- -----------------------------------------------------------
-- 5. 商品图片表 Product detail images
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS fm_product_image (
    id         BIGINT       NOT NULL PRIMARY KEY COMMENT '图片ID (雪花) Image ID (Snowflake)',
    product_id BIGINT       NOT NULL COMMENT '所属商品ID Belonging product ID',
    image_url  VARCHAR(500) NOT NULL COMMENT '图片URL Image URL',
    sort_order INT          DEFAULT 0 COMMENT '排序号 Display sort order',
    create_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间 createTime',
    edit_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间 editTime',
    update_time TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间 updateTime',
    is_delete   TINYINT      DEFAULT 0 NOT NULL COMMENT '是否删除 0=否 1=是 isDelete'
);
COMMENT ON TABLE fm_product_image IS '商品图片表 一个商品可有多张图 Product detail images (multiple per product)';

-- -----------------------------------------------------------
-- 6. 购物车表 Shopping cart
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS fm_cart_item (
    id         BIGINT   NOT NULL PRIMARY KEY COMMENT '购物车项ID (雪花) Cart item ID (Snowflake)',
    user_id    BIGINT   NOT NULL COMMENT '买家用户ID Buyer user ID',
    product_id BIGINT   NOT NULL COMMENT '商品ID Product ID',
    quantity   INT      DEFAULT 1 COMMENT '数量 Quantity in cart',
    selected   TINYINT  DEFAULT 1 COMMENT '是否选中结算 0=否 1=是 Selected for checkout: 0=no 1=yes',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间 createTime',
    edit_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间 editTime',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间 updateTime',
    is_delete   TINYINT   DEFAULT 0 NOT NULL COMMENT '是否删除 0=否 1=是 isDelete'
);
COMMENT ON TABLE fm_cart_item IS '购物车表 买家购物车 Buyer shopping cart items';

-- -----------------------------------------------------------
-- 7. 订单表 Orders
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS fm_order (
    id               BIGINT         NOT NULL PRIMARY KEY COMMENT '订单ID (雪花) Order ID (Snowflake)',
    order_no         VARCHAR(32)    NOT NULL COMMENT '订单号 (用户可见) Order number (visible to user)',
    user_id          BIGINT         NOT NULL COMMENT '买家用户ID Buyer user ID',
    store_id         BIGINT         NOT NULL COMMENT '店铺ID Store ID',
    total_amount     DECIMAL(10,2)  NOT NULL COMMENT '订单总金额 Order total amount',
    status           TINYINT        DEFAULT 0 COMMENT '状态 0=待支付 1=已支付 2=已发货 3=已收货 4=已完成 5=已取消 Status: 0=pending_pay 1=paid 2=shipped 3=received 4=completed 5=cancelled',
    receiver_name    VARCHAR(50)    DEFAULT NULL COMMENT '收货人姓名 Receiver name',
    receiver_phone   VARCHAR(20)    DEFAULT NULL COMMENT '收货人电话 Receiver phone',
    receiver_address VARCHAR(500)   DEFAULT NULL COMMENT '收货地址 Receiver address',
    pay_time         TIMESTAMP      DEFAULT NULL COMMENT '支付时间 Payment timestamp',
    ship_time        TIMESTAMP      DEFAULT NULL COMMENT '发货时间 Shipment timestamp',
    receive_time     TIMESTAMP      DEFAULT NULL COMMENT '收货时间 Receipt timestamp',
    complete_time    TIMESTAMP      DEFAULT NULL COMMENT '完成时间 Completion timestamp',
    cancel_time      TIMESTAMP      DEFAULT NULL COMMENT '取消时间 Cancellation timestamp',
    cancel_reason    VARCHAR(500)   DEFAULT NULL COMMENT '取消原因 Cancellation reason',
    create_time       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间 createTime',
    edit_time         TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间 editTime',
    update_time       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间 updateTime',
    is_delete         TINYINT        DEFAULT 0 NOT NULL COMMENT '是否删除 0=否 1=是 isDelete',
    CONSTRAINT uk_fm_order_order_no UNIQUE (order_no)
);
COMMENT ON TABLE fm_order IS '订单表 跟踪完整生命周期 Orders tracking full lifecycle';

-- -----------------------------------------------------------
-- 8. 订单明细表 Order line items
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS fm_order_item (
    id            BIGINT         NOT NULL PRIMARY KEY COMMENT '订单项ID (雪花) Order item ID (Snowflake)',
    order_id      BIGINT         NOT NULL COMMENT '所属订单ID Belonging order ID',
    product_id    BIGINT         NOT NULL COMMENT '商品ID (引用) Product ID (for reference)',
    product_name  VARCHAR(200)   NOT NULL COMMENT '商品名称快照 Product name snapshot',
    product_image VARCHAR(500)   DEFAULT NULL COMMENT '商品主图快照 Product main image snapshot',
    price         DECIMAL(10,2)  NOT NULL COMMENT '下单时单价快照 Unit price snapshot at order time',
    quantity      INT            NOT NULL COMMENT '购买数量 Purchase quantity',
    create_time    TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间 createTime',
    edit_time      TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间 editTime',
    update_time    TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间 updateTime',
    is_delete      TINYINT        DEFAULT 0 NOT NULL COMMENT '是否删除 0=否 1=是 isDelete'
);
COMMENT ON TABLE fm_order_item IS '订单明细表 价格和商品名为下单时快照 Line items within an order (snapshots)';

-- -----------------------------------------------------------
-- 9. 消息表 Buyer-seller messages
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS fm_message (
    id              BIGINT    NOT NULL PRIMARY KEY COMMENT '消息ID (雪花) Message ID (Snowflake)',
    conversation_id VARCHAR(64) COMMENT '会话标识 buyerId_storeId Conversation key e.g. buyerId_storeId',
    sender_id       BIGINT    NOT NULL COMMENT '发送者用户ID Sender user ID',
    receiver_id     BIGINT    NOT NULL COMMENT '接收者用户ID Receiver user ID',
    content         TEXT      NOT NULL COMMENT '消息内容 Message text content',
    is_read         TINYINT   DEFAULT 0 COMMENT '是否已读 0=未读 1=已读 0=unread 1=read',
    create_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间 createTime',
    edit_time        TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间 editTime',
    update_time      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间 updateTime',
    is_delete        TINYINT   DEFAULT 0 NOT NULL COMMENT '是否删除 0=否 1=是 isDelete'
);
COMMENT ON TABLE fm_message IS '消息表 买家卖家沟通 Buyer-seller chat messages';

-- -----------------------------------------------------------
-- 10. 投诉表 Complaints
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS fm_complaint (
    id              BIGINT       NOT NULL PRIMARY KEY COMMENT '投诉ID (雪花) Complaint ID (Snowflake)',
    user_id         BIGINT       NOT NULL COMMENT '投诉人用户ID Complainant (buyer) user ID',
    order_id        BIGINT       DEFAULT NULL COMMENT '关联订单ID Related order ID (nullable)',
    product_id      BIGINT       DEFAULT NULL COMMENT '关联商品ID Related product ID (nullable)',
    store_id        BIGINT       NOT NULL COMMENT '被投诉店铺ID Target store ID',
    return_id       BIGINT       DEFAULT NULL COMMENT '关联退货单ID (退货争议) Related return ID (for return disputes)',
    type            VARCHAR(50)  DEFAULT NULL COMMENT '投诉类型 product_quality/service/delivery/fraud/return_dispute/other Complaint type',
    title           VARCHAR(200) NOT NULL COMMENT '投诉标题 Complaint title',
    content         TEXT         NOT NULL COMMENT '投诉详情 Complaint detail',
    evidence_images VARCHAR(1000) DEFAULT NULL COMMENT '证据图片URL (逗号分隔) Comma-separated evidence image URLs',
    status          TINYINT      DEFAULT 0 COMMENT '状态 0=待处理 1=处理中 2=已解决 3=已驳回 Status: 0=pending 1=in_progress 2=resolved 3=rejected',
    handler_id      BIGINT       DEFAULT NULL COMMENT '处理人管理员ID Admin handler user ID',
    handle_result   TEXT         DEFAULT NULL COMMENT '处理结果 Admin handling result',
    handle_time     TIMESTAMP    DEFAULT NULL COMMENT '处理时间 Handling timestamp',
    create_time      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间 createTime',
    edit_time        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间 editTime',
    update_time      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间 updateTime',
    is_delete        TINYINT      DEFAULT 0 NOT NULL COMMENT '是否删除 0=否 1=是 isDelete'
);
COMMENT ON TABLE fm_complaint IS '投诉表 买家投诉卖家/商品/订单/退货 Complaints filed against stores/products/orders/returns';

-- -----------------------------------------------------------
-- 11. 退货退款表 Return / Refund
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS fm_return_refund (
    id                  BIGINT         NOT NULL PRIMARY KEY COMMENT '退货单ID (雪花) Return ID (Snowflake)',
    return_no           VARCHAR(32)    NOT NULL COMMENT '退货单号 Return number (visible to user)',
    order_id            BIGINT         NOT NULL COMMENT '关联订单ID Related order ID',
    user_id             BIGINT         NOT NULL COMMENT '买家用户ID Buyer user ID',
    store_id            BIGINT         NOT NULL COMMENT '店铺ID Store ID',
    return_reason       TEXT           NOT NULL COMMENT '退货原因 Return reason',
    return_type         TINYINT        DEFAULT 1 COMMENT '退货类型 0=仅退款 1=退货退款 Type: 0=refund only 1=return & refund',
    refund_amount       DECIMAL(10,2)  NOT NULL COMMENT '退款金额 Refund amount',
    status              TINYINT        DEFAULT 0 COMMENT '状态 0=待审核 1=通过 2=拒绝 3=退回中 4=已收货 5=验货中 6=已退款 7=争议中 Status',
    seller_comment      VARCHAR(500)   DEFAULT NULL COMMENT '卖家审核/验货意见 Seller review/inspection comment',
    evidence_images     VARCHAR(1000)  DEFAULT NULL COMMENT '证据图片URL Evidence image URLs',
    admin_handle_result TEXT           DEFAULT NULL COMMENT '管理员仲裁结果 Admin arbitration result',
    ship_back_time      TIMESTAMP      DEFAULT NULL COMMENT '买家寄回时间 Buyer ship-back timestamp',
    seller_receive_time TIMESTAMP      DEFAULT NULL COMMENT '卖家收货时间 Seller receipt timestamp',
    inspect_time        TIMESTAMP      DEFAULT NULL COMMENT '验货时间 Inspection timestamp',
    refund_time         TIMESTAMP      DEFAULT NULL COMMENT '退款时间 Refund timestamp',
    dispute_time        TIMESTAMP      DEFAULT NULL COMMENT '争议发起时间 Dispute creation timestamp',
    create_time          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间 createTime',
    edit_time            TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间 editTime',
    update_time          TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间 updateTime',
    is_delete            TINYINT        DEFAULT 0 NOT NULL COMMENT '是否删除 0=否 1=是 isDelete',
    CONSTRAINT uk_fm_return_refund_no UNIQUE (return_no)
);
COMMENT ON TABLE fm_return_refund IS '退货退款表 Return / refund orders';

-- -----------------------------------------------------------
-- 12. 余额流水表 Balance transaction logs
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS fm_balance_log (
    id             BIGINT         NOT NULL PRIMARY KEY COMMENT '流水ID (雪花) Log ID (Snowflake)',
    user_id        BIGINT         NOT NULL COMMENT '用户ID User ID',
    amount         DECIMAL(10,2)  NOT NULL COMMENT '金额 正=收入 负=支出 Amount: positive=income negative=expense',
    type           VARCHAR(20)    NOT NULL COMMENT '类型 RECHARGE/PAY/REFUND Transaction type',
    order_no       VARCHAR(64)    DEFAULT NULL COMMENT '关联单号 Related order/return number',
    before_balance DECIMAL(10,2)  NOT NULL COMMENT '变动前余额 Balance before',
    after_balance  DECIMAL(10,2)  NOT NULL COMMENT '变动后余额 Balance after',
    remark         VARCHAR(255)   DEFAULT NULL COMMENT '备注 Remark',
    create_time     TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间 createTime',
    edit_time       TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '编辑时间 editTime',
    update_time     TIMESTAMP      DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '更新时间 updateTime',
    is_delete       TINYINT        DEFAULT 0 NOT NULL COMMENT '是否删除 0=否 1=是 isDelete'
);
COMMENT ON TABLE fm_balance_log IS '余额流水表 记录所有余额变动 Balance transaction logs';
