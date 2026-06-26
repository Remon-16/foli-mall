# Requirements Specification — Store Management

**Doc No**: FOLI-REQ-004 | **Version**: 2.0.0 | **Date**: 2026-06-24
**Related Modules**: StoreController, FmStoreServiceImpl

---

## 1. Document Basics

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 1.0.0 | 2026-06-24 | Initial version | Dev Team |
| 2.0.0 | 2026-06-24 | Enterprise format restructure | Dev Team |
| 2.1.0 | 2026-06-26 | Buyers can apply for store (auto-upgrade to seller) | Dev Team |

---

## 2. Business Scope & Roles

### 2.1 Terminology

| Term | Description |
|------|-------------|
| Store Review | Seller applies to open a store; admin must approve before the store can operate |
| Store Ownership | Each store is bound to a single SELLER user; all operations verify ownership |

### 2.2 Permissions

| Function | BUYER | SELLER | ADMIN | Anonymous |
|----------|-------|--------|-------|-----------|
| Browse approved stores | Yes | Yes | Yes | Yes |
| View store detail | Yes | Yes | Yes | Yes |
| Apply for store | Yes (auto-upgrade) | Yes | — | — |
| Update store info | — | Yes (own store) | — | — |
| View my store | — | Yes | — | — |
| Review stores | — | — | Yes | — |

### 2.3 Scope

**In scope**: Store application; info update; admin review (approve/reject); public store browsing.

**Out of scope**: CLOSED(3) status is defined but not yet used (no store closing functionality).

---

## 3. Business Process Flows

### 3.1 Store State Machine

```
  applyStore / updateStore
         │
         ▼
  ┌──────────┐
  │PENDING(0)│ ← updating store info also resets to this state
  │  Pending  │
  └──┬───┬───┘
     │   │
  APPROVED REJECTED
     │   │
     ▼   ▼
  ┌──────────┐  ┌──────────┐
  │APPROVED(1)│  │REJECTED(2)│
  └──────────┘  └──────────┘

  CLOSED(3) — defined but never used
```

### 3.2 Application Flow

```
User applies → verify user exists
  → if BUYER → auto-upgrade to SELLER
  → if ADMIN → reject (admins cannot open stores)
  → check no existing active store (per-user limit: 1)
  → create store record with status PENDING(0)

Admin reviews → check store status is PENDING (state machine enforced)
  → select APPROVED(1) or REJECTED(2) with optional review comment
```

---

## 4. Detailed Functional Requirements

### 4.1 Store Application

**Precondition**: Logged in (BUYER users auto-upgrade to SELLER on application).

**Rules**: BUYER users are auto-upgraded to SELLER role. ADMIN users cannot apply. Max 1 active store per user (checked via SELECT COUNT). Initial status PENDING(0). Store name required.

**Errors**: USER_NOT_FOUND(201004) / NOT_SELLER_ROLE(202005, only thrown for ADMIN) / STORE_LIMIT_EXCEEDED(202001)

### 4.2 Update Store Info

**Precondition**: Store owner (userId match).

**Rules**: Any status can be updated (including rejected). Update resets to PENDING for re-review. Old review comment cleared.

**Errors**: STORE_NOT_FOUND(202003) / FORBIDDEN(200003)

### 4.3 Store Review

**Precondition**: ADMIN role.

**Rules**: Only PENDING stores can be reviewed (state machine enforced). Only APPROVED(1) or REJECTED(2) accepted.

**Errors**: STORE_NOT_FOUND(202003) / STORE_ALREADY_REVIEWED(202002) / INVALID_REVIEW_STATUS(202004)

### 4.4 Public Browsing

**Rules**: Public list returns only APPROVED stores. Store detail has no status restriction. Response includes owner nickname and product count.

---

## 5. Non-Functional Requirements

- Store ownership verified via userId match for all operations
- Per-user store limit enforced via SELECT COUNT query
