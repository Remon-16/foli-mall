# Requirements Specification — Return & Refund

**Doc No**: FOLI-REQ-006 | **Version**: 2.3.0 | **Date**: 2026-06-24
**Related Modules**: ReturnRefundController, FmReturnRefundServiceImpl

---

## 1. Document Basics

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 1.0.0 | 2026-06-24 | Initial version | Dev Team |
| 2.0.0 | 2026-06-24 | Enterprise format restructure | Dev Team |
| 2.1.0 | 2026-06-26 | Added seller complaint against buyer | Dev Team |
| 2.2.0 | 2026-06-26 | Added buyer dispute appeal (after seller rejection) | Dev Team |
| 2.3.0 | 2026-06-26 | Admin arbitration can reject refund | Dev Team |

---

## 2. Business Scope & Roles

### 2.1 Terminology

| Term | Description |
|------|-------------|
| Refund-only | returnType=0; seller approves → direct refund, no return shipment needed |
| Return & refund | returnType=1; buyer ships back → seller inspects → refund |
| Return number | Format: RT + yyyyMMdd + 6 random digits, globally unique |
| Dispute | Seller disputes returned goods during inspection, or buyer disputes seller's rejection; auto-creates a complaint |
| Arbitration | Admin rules on a disputed return; can approve refund or reject |

### 2.2 Permissions

| Function | BUYER | SELLER | ADMIN |
|----------|-------|--------|-------|
| File return | Yes (own COMPLETED orders) | — | — |
| View own returns | Yes | — | — |
| Ship back | Yes (own returns) | — | — |
| Review returns | — | Yes (own store) | — |
| Confirm receipt/inspect | — | Yes (own store) | — |
| File dispute (inspect) | — | Yes (own store) | — |
| File dispute appeal | Yes (own rejected returns) | — | — |
| File complaint against buyer | — | Yes | — |
| Arbitrate dispute | — | — | Yes |

### 2.3 Scope

**In scope**: Return application; seller review; buyer ship-back; inspection & refund; dispute mechanism (seller inspection dispute + buyer rejection appeal); admin arbitration; auto-complaint on dispute; seller complaint against buyer (entry in return flow).

**Out of scope**: INSPECTING(5) intermediate state (defined but unused); partial refunds (always full order amount); non-dispute inspection failure path; stock restoration on refund.

---

## 3. Business Process Flows

### 3.1 Return State Machine

```
  createReturn
       │
       ▼
  ┌──────────────────┐
  │ PENDING_REVIEW(0)│
  │  Pending Review   │
  └──┬───────┬───────┘
     │       │
     │       └── REJECTED(2) → buyerDispute → DISPUTED(7) → handleDispute(refund) → REFUNDED(6)
	     │                                               └→ handleDispute(reject) → REJECTED(2)
     │
  APPROVED(1)
     │
     ├── returnType=0 (refund-only) → REFUNDED(6)
     │
     └── returnType=1 (return & refund)
           │ shipBack
           ▼
     ┌──────────────┐
     │BUYER_SHIPPING│
     │     (3)      │
     └──────┬───────┘
            │ confirmReceipt
            ▼
     ┌──────────────┐
     │SELLER_RECEIVED│
     │     (4)      │
     └──┬───────┬───┘
        │       │
        │       └── dispute → DISPUTED(7) → handleDispute(refund) → REFUNDED(6)
	        │                                 └→ handleDispute(reject) → REJECTED(2)
        │
        └── inspectPass → REFUNDED(6)

  INSPECTING(5) — defined but unused (SELLER_RECEIVED goes directly to REFUNDED/DISPUTED)
```

---

## 4. Detailed Functional Requirements

### 4.1 File Return

**Precondition**: Order status COMPLETED(4), buyer owns the order.

**Rules**: Refund amount = full order amount. Initial status PENDING_REVIEW(0). Two types: refund-only(0) / return & refund(1).

**Errors**: ORDER_NOT_FOUND(204001) / FORBIDDEN(200003) / ORDER_NOT_COMPLETED(204003)

### 4.2 State Transitions

| Operation | Actor | From | To | Side Effects |
|-----------|-------|------|----|-------------|
| Approve (refund-only) | Seller | PENDING_REVIEW | REFUNDED(6) | Refund to buyer |
| Approve (return&refund) | Seller | PENDING_REVIEW | APPROVED(1) | — |
| Reject | Seller | PENDING_REVIEW | REJECTED(2) | No refund |
| Buyer dispute appeal | Buyer | REJECTED(2) | DISPUTED(7) | Auto-create complaint |
| Ship back | Buyer | APPROVED(1) | BUYER_SHIPPING(3) | Record shipBackTime |
| Confirm receipt | Seller | BUYER_SHIPPING(3) | SELLER_RECEIVED(4) | — |
| Inspect pass | Seller | SELLER_RECEIVED(4) | REFUNDED(6) | Refund + record inspectTime |
| Dispute | Seller | SELLER_RECEIVED(4) | DISPUTED(7) | Auto-create complaint |
| Arbitrate (approve) | Admin | DISPUTED(7) | REFUNDED(6) | Refund to buyer |
| Arbitrate (reject) | Admin | DISPUTED(7) | REJECTED(2) | No refund, uphold rejection |

**Errors**: RETURN_NOT_FOUND(208001) / FORBIDDEN(200003) / WRONG_RETURN_STATUS(208002) / RETURN_ALREADY_PROCESSED(208003)

### 4.3 Dispute → Complaint Linkage

**Seller inspection dispute**: Seller disputes after failed inspection → status DISPUTED(7) + auto-create complaint.

**Buyer rejection appeal**: After seller rejects (REJECTED), buyer can file a dispute appeal → status DISPUTED(7) + auto-create complaint.

When dispute is filed: return→DISPUTED(7) + auto-create Complaint(type=return_dispute, status=PENDING, linked via returnId). Admin handles complaint and arbitrates return.

**Known limitation**: Complaint not auto-updated after arbitration.

---

## 5. Non-Functional Requirements

- Refund uses atomic UPDATE (no optimistic lock needed — single-user credit)
- Refund records balance log (REFUND type) with before/after balances
- All state transitions have state machine guards
