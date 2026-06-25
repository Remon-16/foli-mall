# Foli Mall - A Classic Mall Project

> [中文](./README.md) | English

**Foli Mall** is a classic e-commerce platform covering the complete business workflows of three roles: Buyer, Seller, and Admin. It also serves as an **API test case generation agent test target**, providing a feature-rich, edge-case-dense target system for automated test case generation tools.

## Tech Stack

### Backend

| Technology | Version | Purpose |
| ---------- | ------- | ------- |
| Spring Boot | 3.5.15 | Application framework |
| JDK | 17 | Runtime environment |
| MyBatis-Plus | 3.5.9 | ORM / database access |
| H2 Database | - (MySQL compatibility mode) | In-memory database |
| Knife4j | 4.4.0 (OpenAPI 3.0) | API documentation |
| JWT (jjwt) | 0.11.5 | Authentication |
| Hutool | 5.8.26 | Utility library |

### Frontend

| Technology | Version | Purpose |
| ---------- | ------- | ------- |
| Vue 3 | 3.5 | UI framework |
| TypeScript | 6.0 | Type system |
| Ant Design Vue | 4.2 | Component library |
| Vite | 8.1 | Build tool |
| Pinia | 3.0 | State management |
| vue-i18n | 10.0 | Internationalization |
| Axios | 1.18 | HTTP client |

## Features

- **Three roles**: Buyer, Seller, and Admin, each with dedicated UI and permissions
- **Product browsing & search**: Product display, category browsing, keyword search, paginated loading
- **Shopping cart**: Add items, adjust quantities, select items for checkout
- **Order lifecycle**: Place → Pay → Ship → Receive → Complete → Return/Refund, with full timestamp tracking at each stage
- **Store management**: Sellers apply to open stores, manage product listings, review return requests
- **Operational review**: Admin reviews store applications and product listing requests
- **Buyer-seller messaging**: Conversation-based chat between buyers and sellers
- **Complaint handling**: Buyers file complaints against products/orders/services; Admin mediates
- **Balance & recharge**: Account balance management, transaction logs, payment deduction
- **Return & refund**: Return/refund application, seller inspection, dispute arbitration by Admin
- **Internationalization**: Chinese (zh-CN) and English (en-US) language switching

## Quick Start

### Backend (requires JDK 17+)

```bash
cd foli-backend
./mvnw spring-boot:run
```

After startup, visit:

- API docs (Knife4j): <http://localhost:8080/doc.html>
- H2 Console: <http://localhost:8080/h2-console> (**Note: You must change the JDBC URL to `jdbc:h2:mem:foli_mall`**, username `sa`, no password. Or use this [quick link](http://localhost:8080/h2-console?url=jdbc:h2:mem:foli_mall) to pre-fill the JDBC URL)

### Frontend (requires Node.js 18+)

```bash
cd foli-frontend
npm install
npm run dev
```

After startup, visit: <http://localhost:5173>

## Test Accounts

| Username | Password | Role | Balance |
| -------- | -------- | ---- | ------- |
| admin | admin123 | Admin | 99999 |
| seller01 | seller123 | Seller - Digital Expert | 50000 |
| seller02 | seller123 | Seller - Fashion Manager | 50000 |
| seller03 | seller123 | Seller - Food Expert | 50000 |
| seller04 | seller123 | Seller - Home Buyer | 50000 |
| seller05 | seller123 | Seller - Sports Enthusiast | 50000 |
| buyer01 | buyer123 | Buyer | 10000 |
| buyer02 | buyer123 | Buyer | 10000 |

## Project Structure

```text
foli-mall/
├── foli-backend/                  # Backend Spring Boot project
│   └── src/main/java/com/github/foli_backend/
│       ├── annotation/            # Custom annotations (permission checks, etc.)
│       ├── common/                # Common classes (response wrapper, pagination, etc.)
│       ├── config/                # Configuration (Web, MyBatis-Plus, Knife4j, etc.)
│       ├── constant/              # Constants
│       ├── context/               # Context (current user thread-local, etc.)
│       ├── controller/            # Controller layer
│       │   ├── admin/             # Admin endpoints
│       │   └── seller/            # Seller endpoints
│       ├── dto/                   # Data transfer objects
│       │   ├── request/           # Request DTOs
│       │   └── response/          # Response DTOs
│       ├── entity/                # Database entities (12 tables)
│       ├── interceptor/           # Interceptors (JWT authentication, etc.)
│       ├── mapper/                # MyBatis-Plus Mapper interfaces
│       ├── service/               # Service interfaces
│       │   └── impl/              # Service implementations
│       └── utils/                 # Utility classes
├── foli-frontend/                 # Frontend Vue 3 project
│   └── src/
│       ├── api/                   # API request modules
│       ├── assets/                # Static assets
│       │   └── styles/            # Global styles
│       ├── components/            # Shared components
│       ├── composables/           # Composables
│       ├── i18n/                  # Internationalization
│       │   └── locales/           # Language packs (zh-CN, en-US)
│       ├── layouts/               # Layout components
│       ├── router/                # Router configuration
│       ├── stores/                # Pinia state management
│       │   └── modules/           # Store modules
│       ├── types/                 # TypeScript type definitions
│       └── views/                 # Page views
│           ├── account/           # Account balance
│           ├── admin/             # Admin pages
│           ├── auth/              # Login / Register
│           ├── cart/              # Shopping cart
│           ├── complaint/         # Complaints
│           ├── home/              # Home page
│           ├── message/           # Messages
│           ├── order/             # Orders
│           ├── product/           # Products
│           ├── return/            # Returns & refunds
│           ├── seller/            # Seller pages
│           └── store/             # Stores
├── docs/                          # Requirements and design documents
│   ├── zh-CN/                     # Chinese documentation
│   └── en-US/                     # English documentation
└── LICENSE                        # MIT License
```

## API Documentation

After starting the backend, visit the Knife4j API docs:

<http://localhost:8080/doc.html>

All endpoints (approximately 65) are fully documented with Swagger / OpenAPI 3.0 annotations and support online debugging, covering the following modules:

- Authentication (login, register)
- Products (search, pagination, details)
- Shopping Cart (CRUD, selection)
- Orders (create, pay, ship, receive, complete, cancel)
- Stores (application, review)
- Messages (send, list, unread count)
- Complaints (submit, handle, arbitrate)
- Returns & Refunds (apply, review, ship back, inspect, refund, dispute)
- Account (balance, recharge, transaction log)
- File (image upload)
- Categories (tree query)
- Admin (user management, review workflows, arbitration)

## Database

Uses H2 in-memory database in MySQL compatibility mode, auto-initialized on application startup. Contains 12 tables:

| Table | Description |
| ----- | ----------- |
| fm_user | System users (buyers, sellers, admins) |
| fm_store | Seller stores |
| fm_product_category | Product categories (tree structure) |
| fm_product | Products |
| fm_product_image | Product detail images |
| fm_cart_item | Shopping cart items |
| fm_order | Orders |
| fm_order_item | Order line items |
| fm_message | Buyer-seller messages |
| fm_complaint | Complaints |
| fm_return_refund | Return & refund records |
| fm_balance_log | Balance transaction logs |

Schema: `foli-backend/src/main/resources/schema.sql`  
Seed data: auto-inserted by `DataInitializer` on startup (see `foli-backend/src/main/java/.../config/DataInitializer.java`)  
H2 Console: <http://localhost:8080/h2-console> (JDBC URL: `jdbc:h2:mem:foli_mall`, Username: `sa`, no password)

## Documentation

Project requirements and design documents are located in the `docs/` directory, available in two languages:

- `docs/zh-CN/` — Chinese documentation
- `docs/en-US/` — English documentation

## Testing

The project includes comprehensive unit and integration tests covering all 11 Service implementations and key Controller classes.

### Running Tests

```bash
cd foli-backend
./mvnw test
```

### Testing Tech Stack

| Technology | Purpose |
| ---------- | ------- |
| JUnit 5 | Testing framework |
| Mockito | Dependency mocking (including `mockStatic` for static methods) |
| AssertJ | Fluent assertions |
| `@WebMvcTest` | Controller-layer integration tests |
| `@ExtendWith(MockitoExtension.class)` | Service-layer unit tests |

### Test Coverage

| Layer | Test Class | Method Count |
| ----- | ---------- | ------------ |
| Service | FmOrderServiceImplTest | 37 |
| Service | FmReturnRefundServiceImplTest | 25 |
| Service | FmStoreServiceImplTest | 16 |
| Service | FmProductServiceImplTest | 14 |
| Service | FmCartItemServiceImplTest | 13 |
| Service | AccountServiceImplTest | 10 |
| Service | FmUserServiceImplTest | 8 |
| Service | FmMessageServiceImplTest | 6 |
| Service | FmComplaintServiceImplTest | 5 |
| Service | FmProductCategoryServiceImplTest | 5 |
| Service | FileServiceImplTest | 6 |
| Controller | AuthControllerTest | 4 |
| Controller | OrderControllerTest | 6 |
| Controller | AccountControllerTest | 3 |

### Test Dimensions

- **Happy path**: Successful execution of primary business flows
- **Boundary conditions**: null values, empty collections, zero values, edge cases
- **Exception paths**: Business exceptions (`BizCodeEnum`), state machine validation failures
- **Concurrency & idempotency**: Optimistic lock failures (`update` returning 0), duplicate operation prevention

### Test Helpers

`src/test/java/com/github/foli_backend/helper/TestDataFactory.java` — provides static factory methods for all entities, ensuring consistent test data construction.

## License

This project is open-sourced under the [MIT](./LICENSE) license.
