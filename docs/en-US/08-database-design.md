# 08 - Database Design

| Field | Value |
|---|---|
| **Project** | Foli Mall |
| **Version** | 0.0.1-SNAPSHOT |
| **Document Type** | Design Specification |
| **Last Updated** | 2026-06-24 |

---

## Table of Contents

1. [Overview](#1-overview)
2. [Common Field Conventions](#2-common-field-conventions)
3. [Entity-Relationship Diagram](#3-entity-relationship-diagram)
4. [Table 1: fm_user (User)](#4-table-1-fm_user-user)
5. [Table 2: fm_store (Store)](#5-table-2-fm_store-store)
6. [Table 3: fm_product_category (Category)](#6-table-3-fm_product_category-category)
7. [Table 4: fm_product (Product)](#7-table-4-fm_product-product)
8. [Table 5: fm_product_image (Product Image)](#8-table-5-fm_product_image-product-image)
9. [Table 6: fm_cart_item (Cart Item)](#9-table-6-fm_cart_item-cart-item)
10. [Table 7: fm_order (Order)](#10-table-7-fm_order-order)
11. [Table 8: fm_order_item (Order Item)](#11-table-8-fm_order_item-order-item)
12. [Table 9: fm_message (Message)](#12-table-9-fm_message-message)
13. [Table 10: fm_complaint (Complaint)](#13-table-10-fm_complaint-complaint)
14. [Table 11: fm_return_refund (Return/Refund)](#14-table-11-fm_return_refund-returnrefund)
15. [Table 12: fm_balance_log (Balance Log)](#15-table-12-fm_balance_log-balance-log)
16. [Complete DDL](#16-complete-ddl)

---

## 1. Overview

Foli Mall uses an **H2 in-memory database** running in **MySQL compatibility mode**. The database is initialized at application startup via `schema.sql`. The schema consists of **12 tables** designed to support a full-featured e-commerce system.

**Database Connection**:

| Property | Value |
|---|---|
| URL | `jdbc:h2:mem:foli_mall;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false` |
| Driver | `org.h2.Driver` |
| Username | `sa` |
| Password | (empty) |

---

## 2. Common Field Conventions

### 2.1 Primary Keys -- Snowflake IDs

All primary keys use **Snowflake IDs** via MyBatis-Plus `IdType.ASSIGN_ID`. This produces globally unique 64-bit Long integers, avoiding collision across tables. The ID is auto-generated on insert.

### 2.2 Common Timestamp Fields

Every table includes the following auto-managed timestamp fields:

| Field | Type | Auto-Fill Strategy | Description |
|---|---|---|---|
| `createTime` | TIMESTAMP | INSERT | Set when the record is first created |
| `editTime` | TIMESTAMP | INSERT, UPDATE | Set on creation and updated on every modification |
| `updateTime` | TIMESTAMP | INSERT, UPDATE | Set on creation and updated on every modification |

Default value: `CURRENT_TIMESTAMP`

### 2.3 Logical Deletion

| Field | Type | Default | Description |
|---|---|---|---|
| `isDelete` | TINYINT | 0 | Logical deletion flag: 0=not deleted, 1=deleted |

Configured via MyBatis-Plus:
```yaml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: isDelete
      logic-delete-value: 1
      logic-not-delete-value: 0
```

All queries automatically append `WHERE isDelete=0`, and delete operations become `UPDATE SET isDelete=1`.

### 2.4 Naming Conventions

- **Table names**: `fm_` prefix (Foli Mall), lowercase with underscores (e.g., `fm_product_category`)
- **Column names**: `lowerCamelCase` in Java entities, mapped to `SNAKE_CASE` in SQL via MyBatis-Plus (`map-underscore-to-camel-case: true`)
- **Unique constraints**: `uk_{table}_{column}` pattern (e.g., `uk_fm_user_username`)

---

## 3. Entity-Relationship Diagram

```
fm_user (1) ----< (N) fm_store               [owner]
fm_store (1) ----< (N) fm_product             [store's products]
fm_product_category (1) ----< (N) fm_product  [product category]
fm_product (1) ----< (N) fm_product_image     [product images]
fm_product (1) ----< (N) fm_cart_item         [cart references]
fm_user (1) ----< (N) fm_cart_item            [buyer's cart]
fm_user (1) ----< (N) fm_order                [buyer's orders]
fm_store (1) ----< (N) fm_order               [store's orders]
fm_order (1) ----< (N) fm_order_item          [order line items]
fm_user (1) ----< (N) fm_message              [sender]
fm_user (1) ----< (N) fm_message              [receiver]
fm_user (1) ----< (N) fm_complaint            [complainant]
fm_store (1) ----< (N) fm_complaint           [target store]
fm_user (1) ----< (N) fm_return_refund        [buyer's returns]
fm_store (1) ----< (N) fm_return_refund       [store's returns]
fm_order (1) ----< (N) fm_return_refund       [order returns]
fm_user (1) ----< (N) fm_balance_log          [user's balance records]
```

### 3.1 Key Relationships

| Relationship | From | To | Type | Notes |
|---|---|---|---|---|
| User owns Store | `fm_user.id` | `fm_store.user_id` | 1:N | One seller has one store |
| Store owns Products | `fm_store.id` | `fm_product.store_id` | 1:N | One store has many products |
| Category contains Products | `fm_product_category.id` | `fm_product.category_id` | 1:N | Self-referencing tree via `parent_id` |
| Product has Images | `fm_product.id` | `fm_product_image.product_id` | 1:N | Multiple images per product |
| Buyer Cart | `fm_user.id` | `fm_cart_item.user_id` | 1:N | One buyer's shopping cart |
| Orders | `fm_user.id` | `fm_order.user_id` | 1:N | Buyer places orders |
| Order Items | `fm_order.id` | `fm_order_item.order_id` | 1:N | Line items with price snapshots |
| Messages | `fm_user.id` | `fm_message.sender_id` / `receiver_id` | 1:N | Bidirectional messaging |
| Complaints | `fm_user.id` | `fm_complaint.user_id` | 1:N | Buyer files complaints |
| Returns | `fm_order.id` | `fm_return_refund.order_id` | 1:N | Return on completed order |
| Balance Logs | `fm_user.id` | `fm_balance_log.user_id` | 1:N | Audit trail for balance changes |

---

## 4. Table 1: fm_user (User)

Stores all system users: buyers, sellers, and administrators.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, NOT NULL | User ID (Snowflake) |
| `username` | VARCHAR(50) | UNIQUE, NOT NULL | Login username |
| `password` | VARCHAR(255) | NOT NULL | BCrypt-hashed password |
| `nickname` | VARCHAR(50) | DEFAULT NULL | Display nickname |
| `phone` | VARCHAR(20) | DEFAULT NULL | Phone number |
| `email` | VARCHAR(100) | DEFAULT NULL | Email address |
| `avatar` | VARCHAR(255) | DEFAULT NULL | Avatar image URL |
| `balance` | DECIMAL(10,2) | NOT NULL, DEFAULT 0.00 | Account balance |
| `role` | TINYINT | NOT NULL | 0=BUYER, 1=SELLER, 2=ADMIN |
| `status` | TINYINT | DEFAULT 1 | 0=disabled, 1=enabled |
| `createTime` | TIMESTAMP | NOT NULL | Auto-set |
| `editTime` | TIMESTAMP | NOT NULL | Auto-set |
| `updateTime` | TIMESTAMP | NOT NULL | Auto-set |
| `isDelete` | TINYINT | NOT NULL, DEFAULT 0 | Logical delete |

**Unique constraint**: `uk_fm_user_username` on `username`

---

## 5. Table 2: fm_store (Store)

Stores seller-owned shops. Subject to admin review process.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, NOT NULL | Store ID (Snowflake) |
| `user_id` | BIGINT | NOT NULL | Owner user ID (FK to fm_user) |
| `store_name` | VARCHAR(100) | NOT NULL | Store display name |
| `store_logo` | VARCHAR(255) | DEFAULT NULL | Store logo image URL |
| `description` | TEXT | DEFAULT NULL | Store description |
| `status` | TINYINT | DEFAULT 0 | 0=PENDING, 1=APPROVED, 2=REJECTED, 3=CLOSED |
| `review_comment` | VARCHAR(500) | DEFAULT NULL | Admin review comment |
| `createTime` | TIMESTAMP | NOT NULL | Auto-set |
| `editTime` | TIMESTAMP | NOT NULL | Auto-set |
| `updateTime` | TIMESTAMP | NOT NULL | Auto-set |
| `isDelete` | TINYINT | NOT NULL, DEFAULT 0 | Logical delete |

---

## 6. Table 3: fm_product_category (Category)

Tree-structured product categories. Self-referencing via `parent_id`.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, NOT NULL | Category ID (Snowflake) |
| `parent_id` | BIGINT | DEFAULT 0 | Parent category ID; 0=top level |
| `name` | VARCHAR(50) | NOT NULL | Category name |
| `icon` | VARCHAR(255) | DEFAULT NULL | Category icon URL |
| `sort_order` | INT | DEFAULT 0 | Display sort order within same level |
| `status` | TINYINT | DEFAULT 1 | 0=disabled, 1=enabled |
| `createTime` | TIMESTAMP | NOT NULL | Auto-set |
| `editTime` | TIMESTAMP | NOT NULL | Auto-set |
| `updateTime` | TIMESTAMP | NOT NULL | Auto-set |
| `isDelete` | TINYINT | NOT NULL, DEFAULT 0 | Logical delete |

---

## 7. Table 4: fm_product (Product)

Products published by sellers, subject to admin review.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, NOT NULL | Product ID (Snowflake) |
| `store_id` | BIGINT | NOT NULL | FK to fm_store |
| `category_id` | BIGINT | NOT NULL | FK to fm_product_category |
| `name` | VARCHAR(200) | NOT NULL | Product name |
| `description` | TEXT | DEFAULT NULL | Product description |
| `main_image` | VARCHAR(500) | DEFAULT NULL | Main product image URL |
| `price` | DECIMAL(10,2) | NOT NULL | Unit price |
| `stock` | INT | DEFAULT 0 | Available stock quantity |
| `status` | TINYINT | DEFAULT 0 | 0=DRAFT, 1=PENDING_REVIEW, 2=APPROVED, 3=REJECTED, 4=OFF_SHELF |
| `review_comment` | VARCHAR(500) | DEFAULT NULL | Admin review comment |
| `sales_count` | INT | DEFAULT 0 | Cumulative sales count |
| `createTime` | TIMESTAMP | NOT NULL | Auto-set |
| `editTime` | TIMESTAMP | NOT NULL | Auto-set |
| `updateTime` | TIMESTAMP | NOT NULL | Auto-set |
| `isDelete` | TINYINT | NOT NULL, DEFAULT 0 | Logical delete |

---

## 8. Table 5: fm_product_image (Product Image)

Multiple images per product (in addition to the main image on fm_product).

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, NOT NULL | Image ID (Snowflake) |
| `product_id` | BIGINT | NOT NULL | FK to fm_product |
| `image_url` | VARCHAR(500) | NOT NULL | Image URL |
| `sort_order` | INT | DEFAULT 0 | Display sort order |
| `createTime` | TIMESTAMP | NOT NULL | Auto-set |
| `editTime` | TIMESTAMP | NOT NULL | Auto-set |
| `updateTime` | TIMESTAMP | NOT NULL | Auto-set |
| `isDelete` | TINYINT | NOT NULL, DEFAULT 0 | Logical delete |

---

## 9. Table 6: fm_cart_item (Cart Item)

Buyer shopping cart. Each row represents one product in a buyer's cart.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, NOT NULL | Cart item ID (Snowflake) |
| `user_id` | BIGINT | NOT NULL | FK to fm_user (buyer) |
| `product_id` | BIGINT | NOT NULL | FK to fm_product |
| `quantity` | INT | DEFAULT 1 | Quantity in cart |
| `selected` | TINYINT | DEFAULT 1 | Selected for checkout: 0=no, 1=yes |
| `createTime` | TIMESTAMP | NOT NULL | Auto-set |
| `editTime` | TIMESTAMP | NOT NULL | Auto-set |
| `updateTime` | TIMESTAMP | NOT NULL | Auto-set |
| `isDelete` | TINYINT | NOT NULL, DEFAULT 0 | Logical delete |

---

## 10. Table 7: fm_order (Order)

Orders tracking the full lifecycle from creation to completion/cancellation.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, NOT NULL | Order ID (Snowflake) |
| `order_no` | VARCHAR(32) | UNIQUE, NOT NULL | Human-readable order number |
| `user_id` | BIGINT | NOT NULL | FK to fm_user (buyer) |
| `store_id` | BIGINT | NOT NULL | FK to fm_store |
| `total_amount` | DECIMAL(10,2) | NOT NULL | Total order amount |
| `status` | TINYINT | DEFAULT 0 | 0=PENDING_PAY, 1=PAID, 2=SHIPPED, 3=RECEIVED, 4=COMPLETED, 5=CANCELLED |
| `receiver_name` | VARCHAR(50) | DEFAULT NULL | Shipping receiver name |
| `receiver_phone` | VARCHAR(20) | DEFAULT NULL | Shipping receiver phone |
| `receiver_address` | VARCHAR(500) | DEFAULT NULL | Shipping address |
| `pay_time` | TIMESTAMP | DEFAULT NULL | Payment timestamp |
| `ship_time` | TIMESTAMP | DEFAULT NULL | Shipment timestamp |
| `receive_time` | TIMESTAMP | DEFAULT NULL | Receipt timestamp |
| `complete_time` | TIMESTAMP | DEFAULT NULL | Completion timestamp |
| `cancel_time` | TIMESTAMP | DEFAULT NULL | Cancellation timestamp |
| `cancel_reason` | VARCHAR(500) | DEFAULT NULL | Cancellation reason |
| `createTime` | TIMESTAMP | NOT NULL | Auto-set |
| `editTime` | TIMESTAMP | NOT NULL | Auto-set |
| `updateTime` | TIMESTAMP | NOT NULL | Auto-set |
| `isDelete` | TINYINT | NOT NULL, DEFAULT 0 | Logical delete |

**Unique constraint**: `uk_fm_order_order_no` on `order_no`

---

## 11. Table 8: fm_order_item (Order Item)

Order line items with price and name snapshots captured at the time of order placement.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, NOT NULL | Order item ID (Snowflake) |
| `order_id` | BIGINT | NOT NULL | FK to fm_order |
| `product_id` | BIGINT | NOT NULL | FK to fm_product (reference) |
| `product_name` | VARCHAR(200) | NOT NULL | Product name snapshot |
| `product_image` | VARCHAR(500) | DEFAULT NULL | Product main image snapshot |
| `price` | DECIMAL(10,2) | NOT NULL | Unit price snapshot |
| `quantity` | INT | NOT NULL | Purchase quantity |
| `createTime` | TIMESTAMP | NOT NULL | Auto-set |
| `editTime` | TIMESTAMP | NOT NULL | Auto-set |
| `updateTime` | TIMESTAMP | NOT NULL | Auto-set |
| `isDelete` | TINYINT | NOT NULL, DEFAULT 0 | Logical delete |

**Design Note**: The `product_name`, `product_image`, and `price` fields are **snapshots** -- they capture the values at order time and do not change if the product is later updated. The `product_id` is kept as a reference for linking back to the original product.

---

## 12. Table 9: fm_message (Message)

Buyer-seller chat messages organized by conversation.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, NOT NULL | Message ID (Snowflake) |
| `conversation_id` | VARCHAR(64) | DEFAULT NULL | Conversation key (e.g., `buyerId_storeId`) |
| `sender_id` | BIGINT | NOT NULL | Sender user ID |
| `receiver_id` | BIGINT | NOT NULL | Receiver user ID |
| `content` | TEXT | NOT NULL | Message text content |
| `is_read` | TINYINT | DEFAULT 0 | 0=unread, 1=read |
| `createTime` | TIMESTAMP | NOT NULL | Auto-set |
| `editTime` | TIMESTAMP | NOT NULL | Auto-set |
| `updateTime` | TIMESTAMP | NOT NULL | Auto-set |
| `isDelete` | TINYINT | NOT NULL, DEFAULT 0 | Logical delete |

---

## 13. Table 10: fm_complaint (Complaint)

Complaints filed by buyers against stores/products/orders/returns.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, NOT NULL | Complaint ID (Snowflake) |
| `user_id` | BIGINT | NOT NULL | Complainant (buyer) user ID |
| `order_id` | BIGINT | DEFAULT NULL | Related order ID (nullable) |
| `product_id` | BIGINT | DEFAULT NULL | Related product ID (nullable) |
| `store_id` | BIGINT | NOT NULL | Target store ID |
| `return_id` | BIGINT | DEFAULT NULL | Related return ID (for return disputes) |
| `type` | VARCHAR(50) | DEFAULT NULL | Type: product_quality/service/delivery/fraud/return_dispute/other |
| `title` | VARCHAR(200) | NOT NULL | Complaint title |
| `content` | TEXT | NOT NULL | Detailed complaint description |
| `evidence_images` | VARCHAR(1000) | DEFAULT NULL | Comma-separated image URLs |
| `status` | TINYINT | DEFAULT 0 | 0=PENDING, 1=IN_PROGRESS, 2=RESOLVED, 3=REJECTED |
| `handler_id` | BIGINT | DEFAULT NULL | Admin handler user ID |
| `handle_result` | TEXT | DEFAULT NULL | Admin handling result |
| `handle_time` | TIMESTAMP | DEFAULT NULL | Handling timestamp |
| `createTime` | TIMESTAMP | NOT NULL | Auto-set |
| `editTime` | TIMESTAMP | NOT NULL | Auto-set |
| `updateTime` | TIMESTAMP | NOT NULL | Auto-set |
| `isDelete` | TINYINT | NOT NULL, DEFAULT 0 | Logical delete |

---

## 14. Table 11: fm_return_refund (Return/Refund)

Return and refund requests with full lifecycle tracking.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, NOT NULL | Return ID (Snowflake) |
| `return_no` | VARCHAR(32) | UNIQUE, NOT NULL | Return number |
| `order_id` | BIGINT | NOT NULL | FK to fm_order |
| `user_id` | BIGINT | NOT NULL | FK to fm_user (buyer) |
| `store_id` | BIGINT | NOT NULL | FK to fm_store |
| `return_reason` | TEXT | NOT NULL | Return reason |
| `return_type` | TINYINT | DEFAULT 1 | 0=refund only, 1=return & refund |
| `refund_amount` | DECIMAL(10,2) | NOT NULL | Requested refund amount |
| `status` | TINYINT | DEFAULT 0 | 0=PENDING_REVIEW, 1=APPROVED, 2=REJECTED, 3=BUYER_SHIPPING, 4=SELLER_RECEIVED, 5=INSPECTING, 6=REFUNDED, 7=DISPUTED |
| `seller_comment` | VARCHAR(500) | DEFAULT NULL | Seller review/inspection comment |
| `evidence_images` | VARCHAR(1000) | DEFAULT NULL | Evidence image URLs |
| `admin_handle_result` | TEXT | DEFAULT NULL | Admin arbitration result |
| `ship_back_time` | TIMESTAMP | DEFAULT NULL | Buyer ship-back timestamp |
| `seller_receive_time` | TIMESTAMP | DEFAULT NULL | Seller receipt timestamp |
| `inspect_time` | TIMESTAMP | DEFAULT NULL | Inspection timestamp |
| `refund_time` | TIMESTAMP | DEFAULT NULL | Refund timestamp |
| `dispute_time` | TIMESTAMP | DEFAULT NULL | Dispute creation timestamp |
| `createTime` | TIMESTAMP | NOT NULL | Auto-set |
| `editTime` | TIMESTAMP | NOT NULL | Auto-set |
| `updateTime` | TIMESTAMP | NOT NULL | Auto-set |
| `isDelete` | TINYINT | NOT NULL, DEFAULT 0 | Logical delete |

**Unique constraint**: `uk_fm_return_refund_no` on `return_no`

---

## 15. Table 12: fm_balance_log (Balance Log)

Audit trail for all balance changes (recharge, payment, refund).

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | BIGINT | PK, NOT NULL | Log ID (Snowflake) |
| `user_id` | BIGINT | NOT NULL | FK to fm_user |
| `amount` | DECIMAL(10,2) | NOT NULL | Positive=income, negative=expense |
| `type` | VARCHAR(20) | NOT NULL | RECHARGE / PAY / REFUND |
| `order_no` | VARCHAR(64) | DEFAULT NULL | Related order or return number |
| `before_balance` | DECIMAL(10,2) | NOT NULL | Balance before transaction |
| `after_balance` | DECIMAL(10,2) | NOT NULL | Balance after transaction |
| `remark` | VARCHAR(255) | DEFAULT NULL | Additional notes |
| `createTime` | TIMESTAMP | NOT NULL | Auto-set |
| `editTime` | TIMESTAMP | NOT NULL | Auto-set |
| `updateTime` | TIMESTAMP | NOT NULL | Auto-set |
| `isDelete` | TINYINT | NOT NULL, DEFAULT 0 | Logical delete |

---

## 16. Complete DDL

The complete DDL is located at `src/main/resources/schema.sql`. All tables are created with `CREATE TABLE IF NOT EXISTS` to allow idempotent initialization. The DDL includes Chinese-English bilingual comments for all columns and tables.

**Key DDL characteristics**:
- All tables use `IF NOT EXISTS` for safe re-execution
- `SET MODE MySQL` for MySQL compatibility in H2
- `COMMENT ON TABLE` statements for table-level documentation
- Unique constraints enforce data integrity (usernames, order numbers, return numbers)
- Default values minimize required fields on insert
- Timestamp fields default to `CURRENT_TIMESTAMP`

### 16.1 Index Strategy

The current schema relies on implicit indexes:

| Index Type | Tables | Purpose |
|---|---|---|
| Primary Key (clustered) | All tables | Snowflake ID lookups |
| Unique Index | `fm_user.username`, `fm_order.order_no`, `fm_return_refund.return_no` | Prevent duplicates |
| Query Optimization (via WHERE) | Foreign key columns (`user_id`, `store_id`, `product_id`, `order_id`, `category_id`) | Join and filter operations |

For a production environment, additional composite indexes on frequently queried columns (e.g., `(store_id, status)` on `fm_product`, `(user_id, status)` on `fm_order`) would be recommended.
