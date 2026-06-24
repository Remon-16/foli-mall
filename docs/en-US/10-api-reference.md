# 10 - API Reference

| Field | Value |
|---|---|
| **Project** | Foli Mall |
| **Version** | 0.0.1-SNAPSHOT |
| **Document Type** | API Reference |
| **Last Updated** | 2026-06-24 |

---

## Table of Contents

1. [API Overview](#1-api-overview)
2. [Authentication](#2-authentication)
3. [Unified Response Format](#3-unified-response-format)
4. [Error Codes](#4-error-codes)
5. [Endpoint Listing by Module](#5-endpoint-listing-by-module)
6. [Knife4j Documentation](#6-knife4j-documentation)

---

## 1. API Overview

Foli Mall exposes approximately **65 RESTful API endpoints** organized into **16 resource groups** (OpenAPI tags). All endpoints use JSON for request/response bodies and follow REST conventions.

### 1.1 Base URL

```
http://localhost:8080
```

All API paths are prefixed with `/api/`.

### 1.2 HTTP Methods

| Method | Usage |
|---|---|
| `GET` | Retrieve resources (search, list, detail) |
| `POST` | Create new resources |
| `PUT` | Update resources or trigger actions (pay, ship, review) |
| `DELETE` | Remove resources (logical deletion) |

---

## 2. Authentication

### 2.1 Token Type

The system uses **JWT Bearer Token** authentication.

### 2.2 Obtaining a Token

Send a POST request to `/api/auth/login` with username and password. The response includes a `token` field.

### 2.3 Using the Token

Include the token in the `Authorization` header for all authenticated requests:

```
Authorization: Bearer <token>
```

### 2.4 Token Expiry

Tokens expire **24 hours** (86,400,000 milliseconds) after issuance. After expiry, re-authenticate via the login endpoint.

### 2.5 Public Endpoints (No Token Required)

| Endpoint | Method | Description |
|---|---|---|
| `/api/auth/register` | POST | User registration |
| `/api/auth/login` | POST | User login |
| `/api/products` | GET | Product search |
| `/api/products/{id}` | GET | Product detail |
| `/api/categories` | GET | Category tree |
| `/api/stores` | GET | Store list |
| `/api/stores/{id}` | GET | Store detail |
| `/api/stores/{id}/products` | GET | Store products |
| `/v3/api-docs/**` | GET | OpenAPI specification |
| `/doc.html` | GET | Knife4j UI |
| `/swagger-ui/**` | GET | Swagger UI |
| `/h2-console` | GET | H2 console (not under `/api/`) |

---

## 3. Unified Response Format

### 3.1 Success Response

```json
{
  "code": 200,
  "message": "success",
  "data": <data>
}
```

### 3.2 Error Response

```json
{
  "code": <error_code>,
  "message": "<error_description>",
  "data": null
}
```

### 3.3 Paginated Response

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "current": 1,
    "size": 20,
    "records": [ ... ]
  }
}
```

---

## 4. Error Codes

| Code | HTTP Status | Meaning |
|---|---|---|
| 200 | 200 OK | Success |
| 400 | 400 Bad Request | Client error (validation failure, business rule violation) |
| 401 | 401 Unauthorized | Missing or invalid JWT token |
| 403 | 403 Forbidden | Valid token but insufficient role permissions |
| 404 | 404 Not Found | Resource not found |
| 500 | 500 Internal Server Error | Unexpected server error |

---

## 5. Endpoint Listing by Module

### 5.1 Auth Management (Tag: `Auth`)

| # | Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|---|
| 1 | POST | `/api/auth/register` | Public | - | Register new user account |
| 2 | POST | `/api/auth/login` | Public | - | Login, get JWT token |
| 3 | GET | `/api/auth/me` | Login | Any | Get current user info |

### 5.2 Account Management (Tag: `Account`)

| # | Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|---|
| 4 | GET | `/api/account/balance` | Login | Any | Query current balance |
| 5 | POST | `/api/account/recharge` | Login | Any | Recharge account balance |
| 6 | GET | `/api/account/balance-logs` | Login | Any | Query balance transaction logs |

### 5.3 Category Management (Tag: `Categories`)

| # | Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|---|
| 7 | GET | `/api/categories` | Public | - | Get category tree |
| 8 | POST | `/api/categories` | Login | ADMIN | Create a category |
| 9 | PUT | `/api/categories/{id}` | Login | ADMIN | Update a category |
| 10 | DELETE | `/api/categories/{id}` | Login | ADMIN | Delete a category |

### 5.4 Product Management (Tag: `Products`)

| # | Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|---|
| 11 | GET | `/api/products` | Public | - | Search products |
| 12 | GET | `/api/products/{id}` | Public | - | Get product detail |

### 5.5 Store Management (Tag: `Stores`)

| # | Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|---|
| 13 | GET | `/api/stores` | Public | - | List approved stores |
| 14 | GET | `/api/stores/{id}` | Public | - | Get store detail |
| 15 | GET | `/api/stores/{id}/products` | Public | - | Get store's products |
| 16 | POST | `/api/stores` | Login | SELLER | Apply to open a store |
| 17 | PUT | `/api/stores/{id}` | Login | SELLER | Update own store info |

### 5.6 Shopping Cart (Tag: `Cart`)

All cart endpoints require login.

| # | Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|---|
| 18 | GET | `/api/cart` | Login | Any | Get cart item list |
| 19 | POST | `/api/cart` | Login | Any | Add item to cart |
| 20 | PUT | `/api/cart/{id}` | Login | Any | Update cart item |
| 21 | DELETE | `/api/cart/{id}` | Login | Any | Remove item from cart |
| 22 | DELETE | `/api/cart` | Login | Any | Clear all cart items |

### 5.7 Order Management (Tag: `Orders`)

All order endpoints require login.

| # | Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|---|
| 23 | POST | `/api/orders` | Login | Any | Create order from cart |
| 24 | GET | `/api/orders` | Login | Any | Buyer order list |
| 25 | GET | `/api/orders/{id}` | Login | Any | Get order detail |
| 26 | PUT | `/api/orders/{id}/pay` | Login | Any | Pay order (balance deduction) |
| 27 | PUT | `/api/orders/{id}/cancel` | Login | Any | Cancel order (PENDING_PAY only) |
| 28 | PUT | `/api/orders/{id}/receive` | Login | Any | Confirm receipt |

### 5.8 Return / Refund (Tag: `Returns`)

All return endpoints require login.

| # | Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|---|
| 29 | POST | `/api/returns` | Login | Any | Create return/refund request |
| 30 | GET | `/api/returns` | Login | Any | Buyer's return list |
| 31 | GET | `/api/returns/{id}` | Login | Any | Get return detail |
| 32 | PUT | `/api/returns/{id}/ship-back` | Login | Any | Ship back goods |

### 5.9 Messaging (Tag: `Messages`)

All messaging endpoints require login.

| # | Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|---|
| 33 | POST | `/api/messages` | Login | Any | Send a message |
| 34 | GET | `/api/messages/conversations` | Login | Any | Get conversations list |
| 35 | GET | `/api/messages/conversation/{id}` | Login | Any | Get conversation messages |
| 36 | PUT | `/api/messages/{id}/read` | Login | Any | Mark message as read |
| 37 | PUT | `/api/messages/conversation/{id}/read-all` | Login | Any | Mark all in conversation as read |

### 5.10 Complaint Management (Tag: `Complaints`)

All complaint endpoints require login.

| # | Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|---|
| 38 | POST | `/api/complaints` | Login | Any | Create a complaint |
| 39 | GET | `/api/complaints` | Login | Any | My complaint list |
| 40 | GET | `/api/complaints/{id}` | Login | Any | Get complaint detail |

### 5.11 File Upload (Tag: `Files`)

| # | Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|---|
| 41 | POST | `/api/files/upload` | Login | Any | Upload image file (multipart) |

### 5.12 Seller Center (Tag: `Seller Center`)

All seller endpoints require login + SELLER role.

| # | Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|---|
| 42 | GET | `/api/seller/stores/my` | Login | SELLER | Get my store detail |
| 43 | POST | `/api/seller/products` | Login | SELLER | Publish new product |
| 44 | GET | `/api/seller/products` | Login | SELLER | List my products |
| 45 | PUT | `/api/seller/products/{id}` | Login | SELLER | Update product (re-review) |
| 46 | DELETE | `/api/seller/products/{id}` | Login | SELLER | Delete product (logical) |
| 47 | PUT | `/api/seller/products/{id}/off-shelf` | Login | SELLER | Take product off shelf |
| 48 | PUT | `/api/seller/products/{id}/on-shelf` | Login | SELLER | Put product back on shelf |
| 49 | GET | `/api/seller/orders` | Login | SELLER | Seller order list |
| 50 | GET | `/api/seller/orders/{id}` | Login | SELLER | Seller order detail |
| 51 | PUT | `/api/seller/orders/{id}/ship` | Login | SELLER | Ship order |
| 52 | GET | `/api/seller/returns` | Login | SELLER | Seller return list |
| 53 | GET | `/api/seller/returns/{id}` | Login | SELLER | Seller return detail |
| 54 | PUT | `/api/seller/returns/{id}/approve` | Login | SELLER | Approve return request |
| 55 | PUT | `/api/seller/returns/{id}/reject` | Login | SELLER | Reject return request |
| 56 | PUT | `/api/seller/returns/{id}/confirm-receipt` | Login | SELLER | Confirm receipt of returned goods |
| 57 | PUT | `/api/seller/returns/{id}/inspect-pass` | Login | SELLER | Inspect passed, auto-refund |
| 58 | PUT | `/api/seller/returns/{id}/dispute` | Login | SELLER | Dispute inspection result |

### 5.13 Admin Dashboard (Tag: `Admin`)

All admin endpoints require login + ADMIN role.

| # | Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|---|
| 59 | GET | `/api/admin/users` | Login | ADMIN | List all users (filterable) |
| 60 | PUT | `/api/admin/users/{id}/status` | Login | ADMIN | Enable/disable user |
| 61 | GET | `/api/admin/stores` | Login | ADMIN | List all stores (filterable) |
| 62 | GET | `/api/admin/stores/pending` | Login | ADMIN | List pending stores |
| 63 | PUT | `/api/admin/stores/{id}/review` | Login | ADMIN | Review store |
| 64 | GET | `/api/admin/products/pending` | Login | ADMIN | List pending products |
| 65 | PUT | `/api/admin/products/{id}/review` | Login | ADMIN | Review product |
| 66 | GET | `/api/admin/products` | Login | ADMIN | List all products |
| 67 | GET | `/api/admin/returns` | Login | ADMIN | List all returns |
| 68 | PUT | `/api/admin/returns/{id}/handle-dispute` | Login | ADMIN | Handle return dispute |
| 69 | GET | `/api/admin/complaints` | Login | ADMIN | List all complaints |
| 70 | PUT | `/api/admin/complaints/{id}/handle` | Login | ADMIN | Handle complaint |

### 5.14 Resource Group Summary

| Group | Tag | Controller(s) | Endpoint Count |
|---|---|---|---|
| Auth | `Auth` | `AuthController` | 3 |
| Account | `Account` | `AccountController` | 3 |
| Categories | `Categories` | `CategoryController` | 4 |
| Products | `Products` | `ProductController` | 2 |
| Stores | `Stores` | `StoreController` | 5 |
| Cart | `Cart` | `CartController` | 5 |
| Orders | `Orders` | `OrderController` | 6 |
| Returns | `Returns` | `ReturnRefundController` | 4 |
| Messages | `Messages` | `MessageController` | 5 |
| Complaints | `Complaints` | `ComplaintController` | 3 |
| Files | `Files` | `FileController` | 1 |
| Seller Center | `Seller Center` | `SellerController` | 17 |
| Admin | `Admin` | `AdminController` | 12 |

---

## 6. Knife4j Documentation

### 6.1 Access

Once the backend is running, open the Knife4j API documentation UI at:

```
http://localhost:8080/doc.html
```

### 6.2 Features

Knife4j (built on SpringDoc OpenAPI 3.0) provides:

- **Interactive API documentation**: Browse all endpoints grouped by tag.
- **Request builder**: Fill in parameters and execute API calls directly from the browser.
- **Schema viewer**: View request and response data models.
- **Authentication support**: Set the `Authorization` header globally for authenticated requests.
- **Export**: Export the OpenAPI specification for use with other tools (Postman, Insomnia, etc.).

### 6.3 Using Knife4j for Testing

1. Open `http://localhost:8080/doc.html`
2. Navigate to the `Auth` group and execute `POST /api/auth/login`
3. Copy the `token` from the response
4. Click the "Authorize" button (lock icon) at the top of the page
5. Enter `Bearer <token>` and click "Authorize"
6. All subsequent requests from the UI will include the token

### 6.4 OpenAPI Specification

The raw OpenAPI 3.0 JSON specification is available at:

```
http://localhost:8080/v3/api-docs
```

This can be imported into tools like Postman, Insomnia, or used with API client generators.

### 6.5 Swagger UI Alternative

An alternative Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```
