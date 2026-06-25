# Foli Mall — Project Overview

**Doc No**: FOLI-REQ-001
**Version**: 2.0.0
**Date**: 2026-06-24
**Classification**: Internal

---

## Table of Contents

1. [Document Basics](#1-document-basics)
2. [Business Scope & Roles](#2-business-scope--roles)
3. [Business Process Flows](#3-business-process-flows)
4. [Non-Functional Requirements](#4-non-functional-requirements)
5. [External Dependencies & Interfaces](#5-external-dependencies--interfaces)
6. [Launch & Release Plan](#6-launch--release-plan)

---

## 1. Document Basics

### 1.1 Revision History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 1.0.0 | 2026-06-24 | Initial version | Dev Team |
| 2.0.0 | 2026-06-24 | Restructured to enterprise format, removed API details | Dev Team |

### 1.2 Project Background

In API automated testing, test case generation and validation requires a fully functional, predictably-stateful system under test as a testing target. Existing open-source e-commerce systems are often overly complex, costly to deploy, and difficult to reset. This project builds a lightweight, zero-dependency, fast-starting e-commerce system specifically designed as a test target for an API test-case generation agent.

### 1.3 Project Goals

- **Zero external dependencies**: H2 in-memory database, no external middleware
- **Fast startup**: Both frontend and backend start within seconds with hot reload
- **Feature complete**: Full e-commerce workflow — registration, product search, cart, order & payment, returns, complaints
- **State resettable**: Restart the backend to restore the initial data state
- **Role separation**: Three built-in roles (BUYER, SELLER, ADMIN) covering tripartite e-commerce collaboration

### 1.4 Terminology

| Term | Description |
|------|-------------|
| Foli Mall | Project name |
| JWT | JSON Web Token — stateless authentication |
| BCrypt | Cross-platform password hashing algorithm |
| H2 | Lightweight Java in-memory database in MySQL compatibility mode |
| RBAC | Role-Based Access Control |
| Snowflake ID | Twitter Snowflake distributed unique ID algorithm |
| Optimistic Locking | Concurrency control via conditional UPDATE statements |
| Business Status Code | 6-digit string code (ABBCCC format) in the response body indicating business result |

---

## 2. Business Scope & Roles

### 2.1 Domain Model

| Term | Description |
|------|-------------|
| BUYER | Consumer — browses products, manages cart, places orders, files returns/complaints |
| SELLER | Store operator — opens stores, publishes products, manages orders, processes returns |
| ADMIN | Platform operator — reviews stores/products, handles complaints, arbitrates disputes |
| Store | A seller's shop, subject to admin approval before operating |
| Product | An item listed in a store, publicly visible only after admin approval |
| Order | A purchase record tracking the pay→ship→receive→complete lifecycle |
| Return | A return/refund request with two types: refund-only and return-and-refund |
| Complaint | A buyer's grievance ticket, optionally linked to order/product/store/return |
| Balance | User account funds used for order payment, supporting recharge and refund |
| BalanceLog | Record of every balance change with before/after amounts and reference numbers |

### 2.2 Roles & Permissions (RBAC)

| Code | Role |
|------|------|
| 0 | BUYER |
| 1 | SELLER |
| 2 | ADMIN |

**Permission Matrix**:

| Module | BUYER | SELLER | ADMIN |
|--------|-------|--------|-------|
| Browse approved products | Yes | Yes | Yes |
| Cart management | Yes | — | — |
| Create/pay orders | Yes | — | — |
| Confirm receipt | Yes | — | — |
| File returns/complaints | Yes | — | — |
| Balance recharge & logs | Yes | Yes | Yes |
| Open store | — | Yes (max 1) | — |
| Manage own store/products | — | Yes | — |
| Process own store orders | — | Yes | — |
| Review stores/products | — | — | Yes |
| Manage users | — | — | Yes |
| Handle complaints / arbitrate disputes | — | — | Yes |
| Manage categories | — | — | Yes |

### 2.3 Scope

**In scope**: Full buyer-seller-admin collaboration workflow; JWT authentication; two-level category tree; store/product review; shopping cart; order lifecycle with balance payment; return/refund (two types); buyer-seller messaging; complaint handling; dispute-to-complaint auto-linkage; balance recharge & logs; role-based access control; H2 in-memory database with auto-initialization.

**Out of scope**: Third-party payment integration; SMS/email notifications; coupons/promotions; logistics tracking; analytics & reporting; distributed deployment; production security hardening (rate limiting, data masking, anti-replay).

---

## 3. Business Process Flows

### 3.1 System Overview

```
BUYER                       SELLER                       ADMIN
─────                       ──────                       ─────
Register/Login              Register/Login               Login
  │                           │                           │
Browse products ←────────── Publish products ──────────→ Review products
  │                           │ (pending)                  │
Add to cart                   │                           │
  │                           │ ←────────────────────── Approved
Place order, pay              │                           │
  │                           │                           │
Order PAID ────────────────→ Ship ────────────────────→ (view)
  │                           │                           │
Confirm receipt               │                           │
  │                           │                           │
Order COMPLETED                │                           │
  │                           │                           │
File return ────────────────→ Review return               │
  │                           │ (approve/reject)           │
  │                           │                           │
(refund-only→credited)        │                           │
(return&refund→ship→inspect)  │ ←── dispute ────────────→ Arbitrate
  │                           │                           │
File complaint ─────────────────────────────────────────→ Handle
```

---

## 4. Non-Functional Requirements

### 4.1 Performance

This project is a testing target — no hard QPS/RT targets. All queries complete in milliseconds on H2 in-memory DB. No external network calls. Startup target: <10 seconds (including DB init and seed data).

### 4.2 Availability

Single-instance deployment. No high-availability requirements. Restart resets all data.

### 4.3 Security

| Measure | Implementation |
|---------|---------------|
| Password storage | BCrypt one-way hash |
| Authentication | JWT Bearer token, 24h expiry, HS256 |
| Authorization | @RequireRole annotation, interceptor-enforced |
| Input validation | Jakarta Bean Validation |
| File upload | image/* only, max 10MB, UUID filenames |
| Data isolation | Sellers can only operate their own stores/products/orders |
| Concurrency | Optimistic locking for balance and stock deduction |

### 4.4 Compatibility

JDK 17+. H2 in MySQL compatibility mode — DDL is portable to MySQL. No legacy data to migrate.

---

## 5. External Dependencies & Interfaces

### 5.1 Third-Party Dependencies

**None.** Zero external dependencies.

### 5.2 Provided Services

RESTful API with unified response format:
- Success: `{"code":"100000","message":"success","data":{...}}`
- Business failure: `{"code":"2BBCCC","message":"error description","data":null}` (HTTP 200)
- System error: `{"code":"300001","message":"Internal server error","data":null}` (HTTP 500)

---

## 6. Launch & Release Plan

### 6.1 Data Initialization

On first startup:
1. **Schema**: `foli-backend/src/main/resources/schema.sql` creates 12 tables
2. **Seed data**: `DataInitializer` writes 8 users, 22 categories, 5 stores, 60 products with images

All passwords are BCrypt-hashed. IDs are dynamically generated via Snowflake algorithm.

### 6.2 Startup

```bash
cd foli-backend && ./mvnw spring-boot:run
```

- API docs: http://localhost:8080/doc.html
- H2 Console: http://localhost:8080/h2-console

### 6.3 Rollback

Restarting the application resets all data to initial state (H2 in-memory database). No backup or rollback scripts needed.
