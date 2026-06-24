# API 接口简要说明

**文档编号**: FOLI-REQ-010
**版本**: 1.0.0
**编制日期**: 2026-06-24

---

## 目录

1. [API 概述](#1-api-概述)
2. [认证说明](#2-认证说明)
3. [统一响应格式](#3-统一响应格式)
4. [分页响应格式](#4-分页响应格式)
5. [接口模块索引](#5-接口模块索引)
6. [各模块端点列表](#6-各模块端点列表)
7. [Knife4j 文档入口](#7-knife4j-文档入口)

---

## 1. API 概述

Foli Mall 后端共提供约 **65 个 REST API 端点**，分布在 **11 个 Controller** 中，覆盖 **16 个资源模块**。所有接口遵循 RESTful 设计风格，使用 JSON 作为数据交换格式。

### 1.1 整体统计

| 指标 | 数值 |
|---|---|
| Controller 数量 | 11 |
| 资源模块数量 | 16（含管理端子模块） |
| API 端点总数 | 约 65 |
| 公开接口 | 约 12 |
| 需认证接口 | 约 53 |
| 管理员专用接口 | 约 10 |
| 卖家专用接口 | 约 8 |

### 1.2 接口设计约定

| 约定 | 说明 |
|---|---|
| 基础路径 | 所有 API 以 `/api/` 为前缀 |
| 资源命名 | 使用复数形式，如 `/api/products`、`/api/orders` |
| HTTP 方法 | GET=查询, POST=创建, PUT=更新/操作, DELETE=删除 |
| 路径参数 | 资源 ID 使用路径变量 `/{id}` |
| 子资源操作 | 使用 `/{id}/action` 格式，如 `/{id}/pay` |
| 版本管理 | 当前无版本号前缀，未来可添加 `/api/v1/` |

---

## 2. 认证说明

### 2.1 JWT Bearer Token

系统使用 JWT (JSON Web Token) 进行无状态认证。

**获取 Token**：调用 `POST /api/auth/login` 接口。

**使用 Token**：在请求头中添加：

```
Authorization: Bearer <token>
```

### 2.2 Token 生命周期

| 属性 | 值 |
|---|---|
| 签发方式 | 登录时签发 |
| 有效期 | 24 小时（可配置） |
| 刷新机制 | 当前版本需重新登录 |
| 销毁方式 | 客户端丢弃 Token 即可 |

### 2.3 认证异常

| HTTP 状态码 | 场景 |
|---|---|
| 401 Unauthorized | 缺少 Token、Token 无效或已过期 |
| 403 Forbidden | 角色权限不足 |

---

## 3. 统一响应格式

所有 API 响应使用 `Result<T>` 统一包装。

### 3.1 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 3.2 失败响应

```json
{
  "code": 400,
  "message": "用户名或密码错误",
  "data": null
}
```

### 3.3 状态码规范

| 状态码 | 说明 |
|---|---|
| 200 | 请求成功 |
| 400 | 客户端请求错误（参数校验、业务规则违反） |
| 401 | 未认证（缺少 Token 或 Token 无效） |
| 403 | 无权限（角色不满足要求） |
| 500 | 服务器内部错误 |

---

## 4. 分页响应格式

分页接口使用 `PageResult<T>` 包装。

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "page": 1,
    "pageSize": 20,
    "records": [ ... ]
  }
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `total` | long | 总记录数 |
| `page` | int | 当前页码 |
| `pageSize` | int | 每页大小 |
| `records` | array | 当前页数据列表 |

---

## 5. 接口模块索引

| 序号 | 模块名称 | 资源路径 | Controller | 端点数量（约） |
|---|---|---|---|---|
| 1 | 认证管理 | `/api/auth` | AuthController | 3 |
| 2 | 账户管理 | `/api/account` | AccountController | 3 |
| 3 | 分类管理 | `/api/categories` | CategoryController | 4 |
| 4 | 商品管理 | `/api/products` | ProductController | 2 (公开) |
| 5 | 商品管理(卖家端) | `/api/seller/products` | (SellerProductController) | 约 8 |
| 6 | 商品管理(管理端) | `/api/admin/products` | (AdminProductController) | 约 3 |
| 7 | 店铺管理 | `/api/stores` | StoreController | 6 |
| 8 | 店铺管理(管理端) | `/api/admin/stores` | (AdminStoreController) | 约 2 |
| 9 | 购物车 | `/api/cart` | CartController | 5 |
| 10 | 订单管理 | `/api/orders` | OrderController | 6 |
| 11 | 订单管理(卖家端) | `/api/seller/orders` | (SellerOrderController) | 约 3 |
| 12 | 退货退款 | `/api/returns` | ReturnRefundController | 4 |
| 13 | 退货退款(卖家端) | `/api/seller/returns` | (SellerReturnController) | 约 3 |
| 14 | 退货退款(管理端) | `/api/admin/returns` | (AdminReturnController) | 约 1 |
| 15 | 消息沟通 | `/api/messages` | MessageController | 5 |
| 16 | 投诉管理 | `/api/complaints` | ComplaintController | 3 |
| 17 | 文件上传 | `/api/files` | FileController | 1 |

---

## 6. 各模块端点列表

### 6.1 认证管理 Auth (`/api/auth`)

| 方法 | 路径 | 认证 | 说明 |
|---|---|---|---|
| POST | `/api/auth/register` | 无 | 用户注册 |
| POST | `/api/auth/login` | 无 | 用户登录 |
| GET | `/api/auth/me` | 需登录 | 获取当前用户信息 |

### 6.2 账户管理 Account (`/api/account`)

| 方法 | 路径 | 认证 | 说明 |
|---|---|---|---|
| GET | `/api/account/balance` | 需登录 | 查询余额 |
| POST | `/api/account/recharge` | 需登录 | 账户充值 |
| GET | `/api/account/balance-logs` | 需登录 | 余额流水（分页） |

### 6.3 分类管理 Categories (`/api/categories`)

| 方法 | 路径 | 认证 | 角色 | 说明 |
|---|---|---|---|---|
| GET | `/api/categories` | 无 | - | 获取分类树 |
| POST | `/api/categories` | 需登录 | ADMIN | 创建分类 |
| PUT | `/api/categories/{id}` | 需登录 | ADMIN | 更新分类 |
| DELETE | `/api/categories/{id}` | 需登录 | ADMIN | 删除分类 |

### 6.4 商品管理 -- 公开接口 Products (`/api/products`)

| 方法 | 路径 | 认证 | 说明 |
|---|---|---|---|
| GET | `/api/products` | 无 | 搜索商品（关键词/分类/价格/排序/分页） |
| GET | `/api/products/{id}` | 无 | 获取商品详情（含图片、店铺、分类） |

### 6.5 商品管理 -- 卖家端 Seller Products

| 方法 | 路径 | 认证 | 角色 | 说明 |
|---|---|---|---|---|
| POST | `/api/seller/products` | 需登录 | SELLER | 发布商品 |
| PUT | `/api/seller/products/{id}` | 需登录 | SELLER | 编辑商品 |
| GET | `/api/seller/products` | 需登录 | SELLER | 卖家商品列表（含各状态） |
| GET | `/api/seller/products/{id}` | 需登录 | SELLER | 卖家查看自己商品详情 |
| DELETE | `/api/seller/products/{id}` | 需登录 | SELLER | 删除商品（逻辑删除） |
| PUT | `/api/seller/products/{id}/shelf` | 需登录 | SELLER | 上架/下架商品 |
| PUT | `/api/seller/products/{id}/stock` | 需登录 | SELLER | 更新库存 |

### 6.6 商品管理 -- 管理端 Admin Products

| 方法 | 路径 | 认证 | 角色 | 说明 |
|---|---|---|---|---|
| GET | `/api/admin/products` | 需登录 | ADMIN | 全部商品列表（含待审核） |
| PUT | `/api/admin/products/{id}/review` | 需登录 | ADMIN | 审核商品（通过/拒绝） |
| PUT | `/api/admin/products/{id}/status` | 需登录 | ADMIN | 强制修改商品状态 |

### 6.7 店铺管理 Stores (`/api/stores`)

| 方法 | 路径 | 认证 | 角色 | 说明 |
|---|---|---|---|---|
| GET | `/api/stores` | 无 | - | 店铺公开列表（分页） |
| GET | `/api/stores/{id}` | 无 | - | 店铺详情（含店主昵称、商品数） |
| GET | `/api/stores/{id}/products` | 无 | - | 店铺商品列表（分页） |
| POST | `/api/stores` | 需登录 | SELLER | 申请开店 |
| PUT | `/api/stores/{id}` | 需登录 | SELLER | 更新店铺信息 |

### 6.8 店铺管理 -- 管理端 Admin Stores

| 方法 | 路径 | 认证 | 角色 | 说明 |
|---|---|---|---|---|
| GET | `/api/admin/stores` | 需登录 | ADMIN | 全部店铺列表（含各状态） |
| PUT | `/api/admin/stores/{id}/review` | 需登录 | ADMIN | 审核店铺（通过/拒绝） |

### 6.9 购物车 Cart (`/api/cart`)

| 方法 | 路径 | 认证 | 说明 |
|---|---|---|---|
| GET | `/api/cart` | 需登录 | 获取购物车列表 |
| POST | `/api/cart` | 需登录 | 添加商品到购物车 |
| PUT | `/api/cart/{id}` | 需登录 | 更新购物车项（数量/选中） |
| DELETE | `/api/cart/{id}` | 需登录 | 删除购物车项 |
| DELETE | `/api/cart` | 需登录 | 清空购物车 |

### 6.10 订单管理 Orders (`/api/orders`)

| 方法 | 路径 | 认证 | 说明 |
|---|---|---|---|
| POST | `/api/orders` | 需登录 | 创建订单（从购物车选中商品下单） |
| GET | `/api/orders` | 需登录 | 买家订单列表（分页，可选状态筛选） |
| GET | `/api/orders/{id}` | 需登录 | 订单详情（含明细） |
| PUT | `/api/orders/{id}/pay` | 需登录 | 支付订单（余额付款） |
| PUT | `/api/orders/{id}/cancel` | 需登录 | 取消订单（仅待支付状态） |
| PUT | `/api/orders/{id}/receive` | 需登录 | 确认收货 |

### 6.11 订单管理 -- 卖家端 Seller Orders

| 方法 | 路径 | 认证 | 角色 | 说明 |
|---|---|---|---|---|
| GET | `/api/seller/orders` | 需登录 | SELLER | 卖家订单列表 |
| PUT | `/api/seller/orders/{id}/ship` | 需登录 | SELLER | 卖家发货 |

### 6.12 退货退款 Returns (`/api/returns`)

| 方法 | 路径 | 认证 | 说明 |
|---|---|---|---|
| POST | `/api/returns` | 需登录 | 创建退货退款申请 |
| GET | `/api/returns` | 需登录 | 买家退货列表（分页） |
| GET | `/api/returns/{id}` | 需登录 | 退货详情 |
| PUT | `/api/returns/{id}/ship-back` | 需登录 | 买家寄回商品 |

### 6.13 退货退款 -- 卖家端 Seller Returns

| 方法 | 路径 | 认证 | 角色 | 说明 |
|---|---|---|---|---|
| GET | `/api/seller/returns` | 需登录 | SELLER | 卖家退货列表 |
| PUT | `/api/seller/returns/{id}/review` | 需登录 | SELLER | 卖家审核退货 |
| PUT | `/api/seller/returns/{id}/receive` | 需登录 | SELLER | 卖家确认收货 |
| PUT | `/api/seller/returns/{id}/inspect` | 需登录 | SELLER | 卖家验货（通过/争议） |

### 6.14 退货退款 -- 管理端 Admin Returns

| 方法 | 路径 | 认证 | 角色 | 说明 |
|---|---|---|---|---|
| PUT | `/api/admin/returns/{id}/dispute` | 需登录 | ADMIN | 管理员争议仲裁 |

### 6.15 消息沟通 Messages (`/api/messages`)

| 方法 | 路径 | 认证 | 说明 |
|---|---|---|---|
| POST | `/api/messages` | 需登录 | 发送消息 |
| GET | `/api/messages/conversations` | 需登录 | 获取会话列表 |
| GET | `/api/messages/conversation/{conversationId}` | 需登录 | 获取会话消息（分页） |
| PUT | `/api/messages/{id}/read` | 需登录 | 标记单条已读 |
| PUT | `/api/messages/conversation/{conversationId}/read-all` | 需登录 | 标记会话全部已读 |

### 6.16 投诉管理 Complaints (`/api/complaints`)

| 方法 | 路径 | 认证 | 说明 |
|---|---|---|---|
| POST | `/api/complaints` | 需登录 | 创建投诉 |
| GET | `/api/complaints` | 需登录 | 我的投诉列表（分页） |
| GET | `/api/complaints/{id}` | 需登录 | 投诉详情 |

### 6.17 投诉管理 -- 管理端 Admin Complaints

| 方法 | 路径 | 认证 | 角色 | 说明 |
|---|---|---|---|---|
| GET | `/api/admin/complaints` | 需登录 | ADMIN | 全部投诉列表 |
| PUT | `/api/admin/complaints/{id}/handle` | 需登录 | ADMIN | 处理投诉 |

### 6.18 用户管理 -- 管理端 Admin Users

| 方法 | 路径 | 认证 | 角色 | 说明 |
|---|---|---|---|---|
| GET | `/api/admin/users` | 需登录 | ADMIN | 用户列表 |
| PUT | `/api/admin/users/{id}/status` | 需登录 | ADMIN | 启用/禁用用户 |

### 6.19 文件上传 Files (`/api/files`)

| 方法 | 路径 | 认证 | 说明 |
|---|---|---|---|
| POST | `/api/files/upload` | 需登录 | 上传文件（图片，最大10MB） |

---

## 7. Knife4j 文档入口

### 7.1 访问方式

项目启动后，通过浏览器访问：

```
http://localhost:8080/doc.html
```

### 7.2 文档特性

- **OpenAPI 3.0 标准**：符合最新的 OpenAPI 规范 (Jakarta)
- **中文界面**：Knife4j 设置 `language: zh_cn`
- **接口分组**：按 Tag 自动分组（认证管理、账户管理、分类管理等）
- **在线调试**：页面内可直接填写参数并发起请求
- **全局认证**：点击 "Authorize" 按钮，输入 `Bearer <token>` 可实现全局 JWT 认证
- **模型展示**：自动展示请求和响应的 JSON Schema

### 7.3 相关 URL

| 资源 | URL |
|---|---|
| Knife4j 文档主页 | `http://localhost:8080/doc.html` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| OpenAPI YAML | `http://localhost:8080/v3/api-docs.yaml` |

---

## 附录

### A. 请求示例

#### 注册

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456","nickname":"测试用户"}'
```

#### 登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'
```

#### 带认证的请求

```bash
TOKEN="<从登录接口获取的token>"

curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

#### 搜索商品

```bash
curl "http://localhost:8080/api/products?keyword=手机&minPrice=1000&maxPrice=5000&sortBy=price_asc&page=1&pageSize=20"
```

#### 添加购物车

```bash
curl -X POST http://localhost:8080/api/cart \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"productId":123456,"quantity":2}'
```

#### 创建订单

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"cartItemIds":[111,222],"receiverName":"张三","receiverPhone":"13800138000","receiverAddress":"北京市朝阳区xx路1号"}'
```

### B. 文档变更记录

| 版本 | 日期 | 变更描述 | 作者 |
|---|---|---|---|
| 1.0.0 | 2026-06-24 | 初始版本 | Foli Mall 项目组 |
