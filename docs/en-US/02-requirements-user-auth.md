# Requirements Specification — User & Authentication

**Doc No**: FOLI-REQ-002 | **Version**: 2.0.0 | **Date**: 2026-06-24
**Related Modules**: AuthController, AccountController, FmUserServiceImpl, AccountServiceImpl

---

## 1. Document Basics

### 1.1 Revision History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 1.0.0 | 2026-06-24 | Initial version | Dev Team |
| 2.0.0 | 2026-06-24 | Enterprise format restructure | Dev Team |

---

## 2. Business Scope & Roles

### 2.1 Terminology

| Term | Description |
|------|-------------|
| JWT Token | Stateless token issued on login, containing userId, role, username; 24h expiry |
| BCrypt | Password hashing algorithm used for registration and login verification |
| ThreadLocal Context | User info stored per-request by the interceptor, cleared on completion |
| Balance | Available funds in user account, used for order payment |

### 2.2 Role Permissions

| Function | BUYER | SELLER | ADMIN |
|----------|-------|--------|-------|
| Register | Yes (defaults to BUYER) | — | — |
| Login | Yes | Yes | Yes |
| View own info / balance | Yes | Yes | Yes |
| Recharge | Yes | Yes | Yes |
| View balance logs | Yes | Yes | Yes |
| Enable/disable users | — | — | Yes |

### 2.3 Scope

**In scope**: Username+password registration; BCrypt password hashing; JWT login; annotation-driven RBAC; balance query & recharge; balance transaction logs.

**Out of scope**: Phone/email verification; OAuth; password reset; MFA; token refresh.

---

## 3. Business Process Flows

### 3.1 Registration

```
Submit registration → validate (username 3-50 chars, password 6-100 chars)
  → check username uniqueness (DB UNIQUE constraint)
  → BCrypt hash password
  → create user (default: BUYER, balance=0, status=active)
  → return user info (password excluded)
```

### 3.2 Login

```
Submit credentials → find by username
  → not found → return "Invalid username or password"
  → BCrypt verify password → mismatch → same error (prevents user enumeration)
  → check status: disabled (0) → "Account has been disabled"
  → issue JWT (payload: userId, role, username; 24h expiry)
  → return token + user info
```

### 3.3 Auth Interceptor Flow

```
Request → check @RequireLogin on class/method
  → public → pass
  → protected → extract Bearer token → missing/malformed → HTTP 401
  → validate token (signature + expiry) → invalid → HTTP 401
  → store userId/role/username in ThreadLocal
  → check @RequireRole → mismatch → HTTP 403
  → proceed → clear ThreadLocal on completion
```

### 3.4 Recharge

```
Submit amount (must be > 0) → query current balance → atomic UPDATE balance = balance + amount
  → record BalanceLog (type=RECHARGE, with before/after balances)
```

---

## 4. Detailed Functional Requirements

### 4.1 Registration

**Business Rules**: Username 3-50 chars, globally unique. Password 6-100 chars, BCrypt hashed. Default role BUYER(0), status active(1), balance 0.00.

**Error codes**: USERNAME_EXISTS(201001)

### 4.2 Login

**Business Rules**: Same error for "not found" and "wrong password" to prevent enumeration. Disabled account (status=0) is rejected.

**Error codes**: INVALID_CREDENTIALS(201002) / ACCOUNT_DISABLED(201003)

### 4.3 Balance Query & Recharge

**Business Rules**: Recharge amount must be > 0. Atomic balance update (no optimistic lock needed — single-user operation). Logs recorded with before/after balances.

**Error codes**: USER_NOT_FOUND(201004) / RECHARGE_FAILED(205003)

### 4.4 User Status Management (Admin)

Admin can set status=0 (disable) or status=1 (enable). Disabled users cannot log in.

---

## 5. Non-Functional Requirements

- Passwords never stored or transmitted in plaintext
- JWT secret is configurable (default value for dev only)
- ThreadLocal is force-cleared after each request to prevent memory leaks
- BCrypt hashing <100ms per operation
