# Requirements Specification — Product & Category

**Doc No**: FOLI-REQ-003 | **Version**: 2.0.0 | **Date**: 2026-06-24
**Related Modules**: ProductController, CategoryController, FmProductServiceImpl, FmProductCategoryServiceImpl

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
| Category | Two-level tree structure; top-level categories contain sub-categories |
| Product Review | Seller-published products require admin approval before public visibility |
| Off-Shelf | Making a product unpurchasable; re-listing requires re-review |
| Stock | Available quantity; deducted on order, restored on cancellation |

### 2.2 Permissions

| Function | BUYER | SELLER | ADMIN | Anonymous |
|----------|-------|--------|-------|-----------|
| View category tree | Yes | Yes | Yes | Yes |
| Manage categories | — | — | Yes | — |
| Search/view approved products | Yes | Yes | Yes | Yes |
| Publish/edit/delete products | — | Yes (own store) | — | — |
| Off-shelf/on-shelf | — | Yes (own store) | — | — |
| Review products | — | — | Yes | — |

### 2.3 Scope

**In scope**: Two-level category tree; product publish/edit/delete; multi-image support; product review workflow; off-shelf & re-list; search (keyword/category/price/sort); product detail.

**Out of scope**: SKU management; product attributes; ratings/reviews. DRAFT(0) status is defined but unused.

---

## 3. Business Process Flows

### 3.1 Product State Machine

```
  publishProduct / updateProduct / onShelf
         │
         ▼
  ┌──────────────────┐
  │ PENDING_REVIEW(1)│ ← edit and re-shelf also enter this state
  │   Pending Review  │
  └────┬─────────┬────┘
       │         │
  APPROVED   REJECTED
       │         │
       ▼         ▼
  ┌─────────┐  ┌──────────┐
  │APPROVED │  │REJECTED  │
  │   (2)   │  │   (3)    │
  └────┬────┘  └──────────┘
       │ offShelf
       ▼
  ┌──────────┐
  │OFF_SHELF │── onShelf → PENDING_REVIEW (re-review)
  │   (4)    │
  └──────────┘
```

Key rules: publish→PENDING_REVIEW directly; edit resets to PENDING_REVIEW; only PENDING_REVIEW can be reviewed (state machine enforced); off-shelf has no pre-state check; re-shelf enters PENDING_REVIEW (not directly APPROVED).

### 3.2 Search Flow

Only APPROVED products → keyword LIKE on name → category filter includes all sub-categories → price range → sort (newest/price_asc/price_desc/sales) → paginated result.

---

## 4. Detailed Functional Requirements

### 4.1 Category Management

**Rules**: Two-level tree (parentId=0 for top level). Only enabled (status=1) categories appear in tree. Cannot delete a category that has children.

**Errors**: CATEGORY_NOT_FOUND(211001) / CATEGORY_PARENT_SELF(211003) / CATEGORY_HAS_CHILDREN(211002)

### 4.2 Product Publishing & Editing

**Precondition**: SELLER role with an approved store.

**Rules**: Name required; price > 0; category required; stock defaults to 0; initial sales=0; initial status PENDING_REVIEW. Editing verifies store ownership and resets status to PENDING_REVIEW, clearing old review comments.

**Errors**: PRODUCT_NOT_FOUND(203001) / FORBIDDEN(200003)

### 4.3 Product Review

**Precondition**: ADMIN role.

**Rules**: Only PENDING_REVIEW products can be reviewed (state machine enforced). Only APPROVED(2) or REJECTED(3) accepted as review status.

**Errors**: PRODUCT_NOT_FOUND(203001) / PRODUCT_ALREADY_REVIEWED(203005) / INVALID_REVIEW_STATUS(202004)

### 4.4 Product Search & Detail

**Search**: Only APPROVED products. Keyword fuzzy match. Category filter includes recursive children. Price range and sorting supported. Store names batch-populated.

**Detail**: No status restriction (anyone can view any product including review_comments).

---

## 5. Non-Functional Requirements

- Ownership verified via storeId match for all seller operations
- Category recursion done in memory (load all categories once)
- Store names batch-queried to avoid N+1 problem
