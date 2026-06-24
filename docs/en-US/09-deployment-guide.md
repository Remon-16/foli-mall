# 09 - Deployment Guide

| Field | Value |
|---|---|
| **Project** | Foli Mall |
| **Version** | 0.0.1-SNAPSHOT |
| **Document Type** | Operations Guide |
| **Last Updated** | 2026-06-24 |

---

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [Project Structure](#2-project-structure)
3. [Backend Setup and Startup](#3-backend-setup-and-startup)
4. [Frontend Setup and Startup](#4-frontend-setup-and-startup)
5. [Access URLs](#5-access-urls)
6. [Configuration Reference](#6-configuration-reference)
7. [Seed Data](#7-seed-data)
8. [Troubleshooting](#8-troubleshooting)

---

## 1. Prerequisites

### 1.1 Required Software

| Software | Minimum Version | Purpose | Verification Command |
|---|---|---|---|
| **JDK** | 17+ | Compile and run the Spring Boot backend | `java -version` |
| **Maven** | 3.9+ | Build and manage Java dependencies | `mvn -version` |
| **Node.js** | 18+ | Run the frontend dev server (if using frontend) | `node -v` |
| **npm** | 9+ (bundled with Node.js) | Frontend package management | `npm -v` |

### 1.2 Optional Tools

| Tool | Purpose |
|---|---|
| **Git** | Version control (the project is a Git repository) |
| **IntelliJ IDEA / VS Code** | IDE for development |
| **Postman / Insomnia** | API testing |
| **H2 Console** | Database inspection (built-in, see section 5) |

---

## 2. Project Structure

```
foli-mall/
  foli-backend/                 # Spring Boot backend (Maven project)
    src/
      main/
        java/
          com/github/foli_backend/
            annotation/         # @RequireLogin, @RequireRole
            common/             # Result, PageResult, BusinessException
            config/             # WebMvcConfig, MyBatisPlusConfig, DataInitializer
            constant/           # Role, Status enums
            context/            # UserContext (ThreadLocal)
            controller/         # REST controllers
              admin/            # Admin-only endpoints
              seller/           # Seller-only endpoints
            dto/                # Request/Response DTOs
            entity/             # MyBatis-Plus entities (12 tables)
            interceptor/        # JWT interceptor
            mapper/             # MyBatis-Plus mappers
            service/            # Service interfaces
            utils/              # JWT, Password utilities
        resources/
          application.yaml      # Application configuration
          schema.sql            # DDL for 12 tables
    pom.xml                     # Maven POM
  docs/
    en-US/                      # English requirement documents (these files)
  foli-frontend/                # Frontend (if available)
```

---

## 3. Backend Setup and Startup

### 3.1 Build the Backend

Navigate to the backend directory and build with Maven:

```bash
cd foli-mall/foli-backend
mvn clean package -DskipTests
```

This will:
1. Download all dependencies defined in `pom.xml`
2. Compile the Java source code
3. Package into `target/foli-backend-0.0.1-SNAPSHOT.jar`

### 3.2 Run the Backend

**Option A: Using Maven Wrapper (recommended)**:

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

**Option B: Using Java directly**:

```bash
java -jar target/foli-backend-0.0.1-SNAPSHOT.jar
```

**Option C: Using Maven**:

```bash
mvn spring-boot:run
```

### 3.3 Startup Sequence

When the backend starts:

1. H2 in-memory database is initialized.
2. `schema.sql` is executed, creating all 12 tables.
3. `DataInitializer` (CommandLineRunner) runs and checks if users exist.
4. If no users exist, seed data is inserted: 8 users, 22 categories, 5 stores, 60 products (+ images).
5. If users already exist (subsequent restarts), seeding is skipped.
6. The server starts listening on port **8080**.

### 3.4 Expected Startup Log Output

```
===== 开始初始化种子数据 / Start seeding initial data =====
[1/4] 创建用户 / Seeding users...
  用户创建完毕 / Users created: admin=..., seller01=..., ...
[2/4] 创建分类 / Seeding categories...
  分类创建完毕 / Categories created: 5 一级 + 17 二级 = 22 total
[3/4] 创建店铺 / Seeding stores...
  店铺创建完毕 / Stores created: store1=..., store2=..., ...
[4/4] 创建商品 / Seeding products...
  商品创建完毕 / Products created: 60 total (12 per store)
===== 种子数据初始化完成 / Seed data initialization complete =====
```

Or on subsequent restarts:

```
===== 数据库已有 8 个用户，跳过种子数据初始化 / DB already has 8 users, skip seeding =====
```

---

## 4. Frontend Setup and Startup

### 4.1 Setup

```bash
cd foli-mall/foli-frontend
npm install
```

This installs the dependencies defined in `package.json`.

### 4.2 Run in Development Mode

```bash
npm run dev
```

The frontend dev server (Vite) starts on port **5173** with hot module replacement.

### 4.3 Build for Production

```bash
npm run build
```

The built files are output to the `dist/` directory and can be served by any static file server or integrated with the Spring Boot static resource handler.

---

## 5. Access URLs

Once both backend and frontend are running, the following URLs are available:

### 5.1 Backend Services (port 8080)

| Service | URL | Description |
|---|---|---|
| **API Base** | `http://localhost:8080` | Base URL for all REST API endpoints |
| **Knife4j API Docs** | `http://localhost:8080/doc.html` | Interactive API documentation UI (Knife4j / Swagger) |
| **OpenAPI JSON** | `http://localhost:8080/v3/api-docs` | OpenAPI 3.0 specification (JSON format) |
| **Swagger UI** | `http://localhost:8080/swagger-ui.html` | Alternative Swagger UI |
| **H2 Console** | `http://localhost:8080/h2-console` | H2 database web console |

### 5.2 H2 Console Login

| Setting | Value |
|---|---|
| **JDBC URL** | `jdbc:h2:mem:foli_mall` |
| **User Name** | `sa` |
| **Password** | (leave empty) |

Note: Ensure the JDBC URL matches (no `MODE=MySQL` or other parameters needed in the console login form).

### 5.3 Frontend (port 5173)

| URL | Description |
|---|---|
| `http://localhost:5173` | Frontend SPA home page |

### 5.4 CORS Configuration

The backend has CORS enabled for all `/api/**` paths with the following settings:

- **Allowed Origins**: All origins (`*` pattern)
- **Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS
- **Allowed Headers**: All headers
- **Allow Credentials**: true
- **Max Age**: 3600 seconds

---

## 6. Configuration Reference

### 6.1 application.yaml Key Settings

| Property | Value | Description |
|---|---|---|
| `server.port` | `8080` | HTTP server port |
| `spring.datasource.url` | `jdbc:h2:mem:foli_mall;MODE=MySQL;...` | H2 in-memory database URL |
| `spring.h2.console.enabled` | `true` | Enable H2 web console |
| `spring.h2.console.path` | `/h2-console` | H2 console path |
| `spring.sql.init.mode` | `always` | Execute schema.sql on every startup |
| `spring.servlet.multipart.max-file-size` | `10MB` | Maximum upload file size |
| `mybatis-plus.global-config.db-config.id-type` | `ASSIGN_ID` | Snowflake ID generation |
| `mybatis-plus.global-config.db-config.logic-delete-field` | `isDelete` | Logical deletion field name |
| `knife4j.enable` | `true` | Enable Knife4j API documentation |
| `knife4j.setting.language` | `zh_cn` | Knife4j UI language (Chinese) |
| `jwt.secret` | `foli-mall-secret-key-2026-...` | JWT signing secret |
| `jwt.expiration` | `86400000` | JWT token expiry in ms (24 hours) |
| `app.upload.path` | `./uploads` | Local file upload directory |

### 6.2 Security Warning

The default JWT secret (`foli-mall-secret-key-2026-please-change-in-production-environment`) is intended for development and testing only. **Change this to a strong, random secret in any production deployment.**

### 6.3 Changing the Port

To run on a different port, modify `server.port` in `application.yaml` or pass it as a command-line argument:

```bash
java -jar target/foli-backend-0.0.1-SNAPSHOT.jar --server.port=9090
```

---

## 7. Seed Data

### 7.1 Test Accounts

All seed accounts use **password**: `password`

| Username | Password | Role | Balance |
|---|---|---|---|
| `admin` | `password` | ADMIN (2) | 99,999.00 |
| `seller01` | `password` | SELLER (1) | 50,000.00 |
| `seller02` | `password` | SELLER (1) | 50,000.00 |
| `seller03` | `password` | SELLER (1) | 50,000.00 |
| `seller04` | `password` | SELLER (1) | 50,000.00 |
| `seller05` | `password` | SELLER (1) | 50,000.00 |
| `buyer01` | `password` | BUYER (0) | 10,000.00 |
| `buyer02` | `password` | BUYER (0) | 10,000.00 |

### 7.2 Quick Login Test

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"buyer01","password":"password"}'
```

---

## 8. Troubleshooting

### 8.1 Port Already in Use

**Symptom**: `Port 8080 is already in use` or `Address already in use`

**Solution**:
```bash
# Windows: Find and kill the process on port 8080
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/macOS:
lsof -i :8080
kill -9 <PID>
```

### 8.2 Java Version Mismatch

**Symptom**: `Unsupported class file major version` or compilation errors

**Solution**: Ensure JDK 17 is installed and `JAVA_HOME` is set correctly:

```bash
# Windows
echo %JAVA_HOME%
java -version

# Linux/macOS
echo $JAVA_HOME
java -version
```

### 8.3 Maven Dependency Issues

**Symptom**: Build fails with dependency resolution errors

**Solution**:
```bash
# Clear local Maven cache and rebuild
mvn clean package -DskipTests -U
```

### 8.4 H2 Console Login Fails

**Symptom**: Cannot connect to H2 database in the console

**Solution**: Use the correct JDBC URL: `jdbc:h2:mem:foli_mall` (do not include `MODE=MySQL` or other parameters in the console login form).

### 8.5 Data Lost After Restart

**Expected behavior**: H2 in-memory database is ephemeral -- all data is lost when the backend stops. This is by design for the test target scenario. The `DataInitializer` re-seeds the data if the user table is empty on the next startup.

For persistent data, switch to a file-based H2 database or MySQL/PostgreSQL by changing the datasource URL in `application.yaml`.

### 8.6 Knife4j API Docs Not Loading

**Symptom**: `/doc.html` shows a blank page or loading error

**Solution**:
1. Ensure the backend has fully started (wait for the startup log to complete).
2. Verify the Knife4j dependency is present: Check `pom.xml` for `knife4j-openapi3-jakarta-spring-boot-starter`.
3. Clear browser cache and try again.
