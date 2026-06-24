# Requirements Specification — Messaging & Complaints

**Doc No**: FOLI-REQ-007 | **Version**: 2.0.0 | **Date**: 2026-06-24
**Related Modules**: MessageController, ComplaintController, FmMessageServiceImpl, FmComplaintServiceImpl

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
| Conversation | Message channel between two users, identified by a deterministic conversation ID |
| Conversation ID | Format: `minUserId_maxUserId` — order-independent |
| Complaint types | product_quality / service / delivery / fraud / return_dispute / other |
| Return dispute complaint | Auto-created when seller disputes a return (type=return_dispute) |

### 2.2 Permissions

| Function | BUYER | SELLER | ADMIN |
|----------|-------|--------|-------|
| Send messages | Yes | Yes | — |
| View own conversations | Yes | Yes | — |
| Mark as read | Yes (own) | Yes (own) | — |
| File complaint | Yes | — | — |
| View own complaints | Yes | — | — |
| View/handle all complaints | — | — | Yes |

### 2.3 Scope

**In scope**: 1-on-1 buyer-seller messaging; conversation grouping with unread counts; complaint creation & handling; auto-complaint on return dispute.

**Out of scope**: Group chat; push notifications/read receipts; IN_PROGRESS(1) complaint status (PENDING can directly become RESOLVED/REJECTED).

---

## 3. Business Process Flows

### 3.1 Complaint State Machine

```
  createComplaint
       │
       ▼
  ┌──────────┐
  │PENDING(0)│
  │  Pending  │
  └────┬─────┘
       │ handleComplaint
       │
       ├── RESOLVED(2)
       │
       └── REJECTED(3)

  IN_PROGRESS(1) — defined but direct PENDING→RESOLVED/REJECTED is allowed
  State guard: RESOLVED/REJECTED complaints cannot be re-handled
```

### 3.2 Messaging Flow

```
Buyer → send message → Seller
  conversationId = min(id1, id2)_max(id1, id2)
  isRead = 0

Conversation list: groups messages by conversationId, returns latest message
  + unread count (where current user is receiver and isRead=0)
  + other party's nickname, sorted by latest message time desc
```

### 3.3 Dispute → Complaint Auto-Creation

```
Seller disputes return → return status → DISPUTED(7)
  → auto-create FmComplaint:
      userId = store owner
      storeId = store
      returnId = return
      type = "return_dispute"
      status = PENDING(0)
```

---

## 4. Detailed Functional Requirements

### 4.1 Send Message

**Precondition**: Logged in.

**Rules**: Conversation ID deterministically generated from both user IDs. Default isRead=0. Returns sender and receiver nicknames.

### 4.2 Conversation List

**Rules**: Queries all messages where user is sender or receiver. Groups by conversationId in memory. Returns latest message, unread count, and other user info per conversation.

### 4.3 Mark as Read

**Rules**: Single: verifies message receiver is current user. Batch: marks all unread messages in conversation where current user is receiver.

**Errors**: MESSAGE_NOT_FOUND(210001) / FORBIDDEN(200003)

### 4.4 Create Complaint

**Precondition**: Logged in.

**Rules**: Optionally links to order/product/store/return. Types include product_quality/service/delivery/fraud/return_dispute/other. Title and content required. Initial status PENDING(0).

### 4.5 Handle Complaint

**Precondition**: ADMIN role.

**Rules**: State machine guard — RESOLVED/REJECTED complaints cannot be re-handled. Records handler, result, and time. Target status: RESOLVED(2) or REJECTED(3).

**Errors**: COMPLAINT_NOT_FOUND(209001) / COMPLAINT_ALREADY_HANDLED(209002)

---

## 5. Non-Functional Requirements

- Message content has no length limit (current implementation)
- Conversation list is aggregated in memory (not DB GROUP BY)
- Auto-created dispute complaints are not auto-updated after admin arbitration
