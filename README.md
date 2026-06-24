# Foli Mall - 经典商城项目

> [English](./README.en.md) | 中文

**Foli Mall** 是一个经典商城项目，涵盖买家、卖家、管理员三种角色完整业务流程的电商平台。该项目同时也是**接口测试用例生成智能体的测试靶场**，为自动化测试用例生成工具提供功能齐全、边界场景丰富的目标系统。

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
| ---- | ---- | ---- |
| Spring Boot | 3.5.15 | 应用框架 |
| JDK | 17 | 运行环境 |
| MyBatis-Plus | 3.5.9 | ORM / 数据库操作 |
| H2 Database | - (MySQL 兼容模式) | 内存数据库 |
| Knife4j | 4.4.0 (OpenAPI 3.0) | API 文档 |
| JWT (jjwt) | 0.11.5 | 身份认证 |
| Hutool | 5.8.26 | 工具类库 |

### 前端

| 技术 | 版本 | 用途 |
| ---- | ---- | ---- |
| Vue 3 | 3.5 | UI 框架 |
| TypeScript | 6.0 | 类型系统 |
| Ant Design Vue | 4.2 | 组件库 |
| Vite | 8.1 | 构建工具 |
| Pinia | 3.0 | 状态管理 |
| vue-i18n | 10.0 | 国际化 |
| Axios | 1.18 | HTTP 客户端 |

## 功能特性

- **三种角色**：买家、卖家、管理员，各自拥有独立操作界面和权限
- **商品浏览与搜索**：商品展示、分类浏览、关键词搜索、分页加载
- **购物车**：添加商品、修改数量、选中结算
- **订单生命周期**：下单 → 支付 → 发货 → 收货 → 完成 → 退货/退款，完整追踪各时间节点
- **店铺管理**：卖家申请开店、管理商品上下架、审核退货申请
- **运营审核**：管理员审核店铺入驻、审核商品上架
- **买卖家沟通**：消息会话，支持买家与卖家实时沟通
- **投诉处理**：买家投诉商品/订单/服务，管理员介入处理
- **余额与充值**：账户余额管理、充值流水记录、支付扣款
- **退货退款**：退货退款申请、卖家审核验货、争议管理员仲裁
- **国际化**：支持中文（zh-CN）和英文（en-US）切换

## 快速开始

### 后端（需要 JDK 17+）

```bash
cd foli-backend
./mvnw spring-boot:run
```

启动后访问：

- API 文档（Knife4j）：<http://localhost:8080/doc.html>
- H2 控制台：<http://localhost:8080/h2-console>

### 前端（需要 Node.js 18+）

```bash
cd foli-frontend
npm install
npm run dev
```

启动后访问：<http://localhost:5173>

## 测试账号

| 用户名 | 密码 | 角色 | 账户余额 |
| ------ | ------ | ------ | -------- |
| admin | admin123 | 管理员 | 99999 |
| seller01 | seller123 | 卖家 | 50000 |
| seller02 | seller123 | 卖家 | 50000 |
| seller03 | seller123 | 卖家 | 50000 |
| seller04 | seller123 | 卖家 | 50000 |
| seller05 | seller123 | 卖家 | 50000 |
| buyer01 | buyer123 | 买家 | 10000 |
| buyer02 | buyer123 | 买家 | 10000 |

## 项目结构

```text
foli-mall/
├── foli-backend/                  # 后端 Spring Boot 项目
│   └── src/main/java/com/github/foli_backend/
│       ├── annotation/            # 自定义注解（权限校验等）
│       ├── common/                # 通用类（结果封装、分页等）
│       ├── config/                # 配置类（Web、MyBatis-Plus、Knife4j 等）
│       ├── constant/              # 常量定义
│       ├── context/               # 上下文（当前用户线程变量等）
│       ├── controller/            # 控制器层
│       │   ├── admin/             # 管理员接口
│       │   └── seller/            # 卖家接口
│       ├── dto/                   # 数据传输对象
│       │   ├── request/           # 请求 DTO
│       │   └── response/          # 响应 DTO
│       ├── entity/                # 数据库实体（12 张表）
│       ├── interceptor/           # 拦截器（JWT 认证等）
│       ├── mapper/                # MyBatis-Plus Mapper 接口
│       ├── service/               # 业务层接口
│       │   └── impl/              # 业务层实现
│       └── utils/                 # 工具类
├── foli-frontend/                 # 前端 Vue 3 项目
│   └── src/
│       ├── api/                   # API 请求封装
│       ├── assets/                # 静态资源
│       │   └── styles/            # 全局样式
│       ├── components/            # 通用组件
│       ├── composables/           # 组合式函数
│       ├── i18n/                  # 国际化
│       │   └── locales/           # 语言包 (zh-CN, en-US)
│       ├── layouts/               # 布局组件
│       ├── router/                # 路由配置
│       ├── stores/                # Pinia 状态管理
│       │   └── modules/           # Store 模块
│       ├── types/                 # TypeScript 类型定义
│       └── views/                 # 页面视图
│           ├── account/           # 账户余额
│           ├── admin/             # 管理员页面
│           ├── auth/              # 登录注册
│           ├── cart/              # 购物车
│           ├── complaint/         # 投诉
│           ├── home/              # 首页
│           ├── message/           # 消息
│           ├── order/             # 订单
│           ├── product/           # 商品
│           ├── return/            # 退货退款
│           ├── seller/            # 卖家页面
│           └── store/             # 店铺
├── docs/                          # 需求及设计文档
│   ├── zh-CN/                     # 中文文档
│   └── en-US/                     # 英文文档
├── sql/                           # 数据库建表及测试数据
│   ├── schema.sql                 # 建表 DDL（12 张表）
│   ├── data.sql                   # 测试数据
│   └── image/                     # 示例商品图片
└── LICENSE                        # MIT 许可证
```

## API 文档

启动后端后，访问 Knife4j 接口文档页面：

<http://localhost:8080/doc.html>

所有接口（约 65 个）均已通过 Swagger / OpenAPI 3.0 注解完整描述，支持在线调试，涵盖以下模块：

- 认证（登录、注册）
- 商品（搜索、分页、详情）
- 购物车（增删改、选中）
- 订单（创建、支付、发货、收货、完成、取消）
- 店铺（申请、审核）
- 消息（发送、列表、未读数）
- 投诉（提交、处理、仲裁）
- 退货退款（申请、审核、退货、验货、退款、争议）
- 账户（余额、充值、流水）
- 文件（图片上传）
- 分类（树形分类查询）
- 管理员（用户管理、审核流转、仲裁）

## 数据库

使用 H2 内存数据库，MySQL 兼容模式，应用启动时自动初始化。包含 12 张表：

| 表名 | 说明 |
| ---- | ---- |
| fm_user | 用户表（买家、卖家、管理员） |
| fm_store | 店铺表 |
| fm_product_category | 商品分类表（树形结构） |
| fm_product | 商品表 |
| fm_product_image | 商品图片表 |
| fm_cart_item | 购物车表 |
| fm_order | 订单表 |
| fm_order_item | 订单明细表 |
| fm_message | 消息表 |
| fm_complaint | 投诉表 |
| fm_return_refund | 退货退款表 |
| fm_balance_log | 余额流水表 |

建表脚本：`sql/schema.sql`  
测试数据：`sql/data.sql`  
H2 控制台：<http://localhost:8080/h2-console>（JDBC URL: `jdbc:h2:mem:foli_mall`，用户名: `sa`，密码留空）

## 文档

项目需求及设计文档位于 `docs/` 目录，提供中英文两个版本：

- `docs/zh-CN/` — 中文文档
- `docs/en-US/` — 英文文档

## 许可证

本项目基于 [MIT](./LICENSE) 许可证开源。
