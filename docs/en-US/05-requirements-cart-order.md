# Requirements Specification — Cart & Order

**Doc No**: FOLI-REQ-005 | **Version**: 2.0.0 | **Date**: 2026-06-24
**Related Modules**: CartController, OrderController, FmCartItemServiceImpl, FmOrderServiceImpl

---

## 1. Document Basics

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 1.0.0 | 2026-06-24 | Initial version | Dev Team |
| 2.0.0 | 2026-06-24 | Enterprise format restructure | Dev Team |

---

## 2. Business Scope & Roles

### 2.1 Terminology

| Term | Description |
|------|-------------|
| Cart dedup | Adding the same product increments quantity instead of creating a new row |
| Selected checkout | Only cart items marked selected=1 participate in order creation |
| Optimistic locking | Atomic deduction via conditional UPDATE (WHERE stock>=qty) |
| Order number | Format: FO + yyyyMMdd + 6 random digits, globally unique |
| Balance payment | Payment deducted directly from buyer's balance at order creation |

### 2.2 Permissions

| Function | BUYER | SELLER | ADMIN |
|----------|-------|--------|-------|
| Cart CRUD | Yes | — | — |
| Create/View own orders | Yes | — | — |
| Pay/Cancel/Receive orders | Yes (own) | — | — |
| View store orders | — | Yes (own store) | — |
| Ship orders | — | Yes (own store) | — |

### 2.3 Scope

**In scope**: Cart CRUD with dedup; one-step order+payment; optimistic lock stock & balance deduction; order lifecycle; balance log recording.

**Out of scope**: PENDING_PAY flow (defined but not the main path — createOrder directly sets PAID); RECEIVED(3) intermediate state (SHIPPED jumps directly to COMPLETED); cross-store cart splitting.

---

## 3. Business Process Flows

### 3.1 Order State Machine

```
  createOrder
       │
       ▼
  ┌──────────┐     payOrder       ┌──────────┐
  │PENDING_PAY│ ─────────────────→ │   PAID   │
  │    (0)    │                    │   (1)    │
  └─────┬─────┘                    └────┬─────┘
        │ cancelOrder                   │ shipOrder
        ▼                               ▼
  ┌──────────┐                    ┌──────────┐
  │CANCELLED │                    │ SHIPPED  │
  │   (5)    │                    │   (2)    │
  └──────────┘                    └────┬─────┘
                                       │ receiveOrder
                                       ▼
                                  ┌──────────┐
                                  │COMPLETED │
                                  │   (4)    │
                                  └──────────┘

  RECEIVED(3) — defined but not used (SHIPPED→COMPLETED directly)
  PENDING_PAY(0) — defined but createOrder creates as PAID immediately
```

### 3.2 Order Creation Flow

```
Get selected cart items → validate each: exists + APPROVED + stock>=qty
  → optimistic deduct stock: UPDATE stock=stock-qty WHERE stock>=qty
  → optimistic deduct balance: UPDATE balance=balance-total WHERE balance>=total
  → create order (PAID, payTime=now)
  → create order items (snapshot: name, image, price)
  → update product sales_count
  → record BalanceLog (PAY type)
  → clear selected cart items
```

---

## 4. Detailed Functional Requirements

### 4.1 Cart Management

**Add to cart**: Product must exist + APPROVED + stock>0. Same product increments quantity. Default quantity=1, selected=1.

**Update**: Can modify quantity and selected status. Stock not re-validated on update.

**Remove/Clear**: Logical delete via @TableLogic.

**Errors**: PRODUCT_NOT_FOUND(203001) / PRODUCT_OFF_SHELF(203004) / INSUFFICIENT_STOCK(203002) / CART_ITEM_NOT_FOUND(207002)

### 4.2 Order Creation & Payment

**Precondition**: Logged in, cart has selected items.

**Key rules**: One-step create+pay (status=PAID). Stock check: `stock >= qty` (not just `stock > 0`). Balance check: `balance >= total`. Both use optimistic locking — if affected rows=0, throw failure.

**Errors**: CART_EMPTY(207001) / INSUFFICIENT_STOCK(203002) / STOCK_DEDUCTION_FAILED(203003) / INSUFFICIENT_BALANCE(205001) / BALANCE_DEDUCTION_FAILED(205002)

### 4.3 State Transitions

| Operation | From | To | Actor | Side Effects |
|-----------|------|----|-------|-------------|
| Pay | PENDING_PAY | PAID | Buyer | Deduct balance + log |
| Cancel | PENDING_PAY | CANCELLED | Buyer | Refund + restore stock + log |
| Ship | PAID | SHIPPED | Seller | Record shipTime |
| Receive | SHIPPED | COMPLETED | Buyer | Record receiveTime + completeTime |

All transitions verified: ownership check + state machine guard.

### 4.4 Order Number Format

Pattern: `FO` + `yyyyMMdd` + 6 random digits. Example: `FO20260624123456`

---

## 5. Non-Functional Requirements

### 5.1 Concurrency Safety
- Stock: optimistic lock (WHERE stock >= qty)
- Balance: optimistic lock (WHERE balance >= amount)
- All within single transaction (@Transactional)

### 5.2 Data Consistency
- Order creation is an all-or-nothing transaction: validate → deduct stock → deduct balance → create order → clear cart → record log
