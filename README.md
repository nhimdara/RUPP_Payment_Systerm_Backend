# Royal University of Phnom Penh (RUPP)
## Department of Computer Science — Mobile & Web Final Project
# RUPP Electronic Tuition Payment & Billing System (Backend)

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/PostgreSQL-14%2B%20%2F%2018-blue.svg)](https://www.postgresql.org/)
[![Security](https://img.shields.io/badge/JWT-RBAC%20Enabled-red.svg)](https://jwt.io/)
[![Swagger](https://img.shields.io/badge/Swagger%20UI-OpenAPI%203.0-green.svg)](http://localhost:8082/swagger-ui.html)
[![Tests](https://img.shields.io/badge/Tests-132%20Passed%20(100%25)-success.svg)]()

A high-performance, enterprise-grade backend REST API service built for managing university academic structures, student tuition calculation, multi-channel payment processing (KHQR / Bakong / ABA / Wing), two-step OTP verification, official computer-generated PDF receipts, financial reporting, and audit log timelines.

---

## 👥 Team Members & Module Division

| # | Name | GitHub / Handle | Role & Module Responsibilities |
|:---:|:---|:---|:---|
| **1** | **Nhim Dara** | [@dara_nhim](https://github.com/dara_nhim) | **Core Foundation & Auth**<br>• Project architecture & PostgreSQL connection<br>• ERD design & database schema mapping<br>• JWT Security setup & Role-based access control (RBAC)<br>• Auth APIs (`/login`, `/register`, `/refresh`, `/me`)<br>• User & Student CRUD endpoints<br>• Swagger / OpenAPI documentation integration |
| **2** | **Taing Leangkhai** | [@leangkhai](https://github.com/leangkhai) | **Academic & Tuition Management**<br>• Major, AcademicYear, and Semester CRUD<br>• TuitionFee setup & dynamic tuition balance calculation logic<br>• Core Payment APIs & verification flow<br>• Receipts generation & export endpoints<br>• Financial reports data services |
| **3** | **Tab Ayutaya** | [@boayuthaya](https://github.com/boayuthaya) | **Testing, QA & Data Seeding**<br>• Seed data SQL scripts for initial setup (`data.sql`, `seed_data.sql`)<br>• Comprehensive Postman Collection & environments<br>• End-to-end integration test suite (32 E2E API tests)<br>• Global exception handling (`@RestControllerAdvice`)<br>• Request body validation checks (`@Valid`) |
| **4** | **Thorn Chhuntheang** | [@chhuntheangthorn](https://github.com/chhuntheangthorn) | **Search, Filtering & Unit Testing**<br>• Multi-param specifications for payment search & reports<br>• Unit tests for payment status state machine (28 tests)<br>• Dynamic pagination and multi-field sorting optimization |
| **5** | **Tang Chihong** | [@chihongtang](https://github.com) | **PDF Engine & Analytics Dashboard**<br>• Official Receipt PDF generation template & exporter (OpenPDF)<br>• Dashboard summary aggregations (daily and monthly revenue metrics)<br>• Database performance indexing for high-efficiency summary queries |
| **6** | **Nuon Vannsonleng** | [@j_oftdy](https://github.com/j_oftdy) | **Payment Security & Verification**<br>• Payment verification audit logs & timeline tracking<br>• Idempotency & duplicate payment prevention guard (`Idempotency-Key`)<br>• Two-step OTP verification challenge endpoints |

---

## 🚀 Key Features

* **Role-Based Access Control (RBAC):** Three distinct roles (`ROLE_ADMIN`, `ROLE_FINANCE_STAFF`, `ROLE_STUDENT`) enforced via stateless JWT filters.
* **Tuition Calculation Engine:** Real-time ledger computation calculating total fees, confirmed payments, remaining balance, and status flags (`PAID`, `PARTIAL`, `UNPAID`).
* **Multi-Channel Payment Ingestion:** Support for `KHQR_BAKONG`, `ABA_PAYWAY`, `WING`, `ACLEDAMOBILE`, and `CASH`.
* **Idempotency & Fraud Prevention:** Automatic duplicate detection for transaction references and optional HTTP `Idempotency-Key` headers returning HTTP `409 Conflict`.
* **Two-Step OTP Challenge:** Verification workflow with 6-digit codes, 5-minute TTL, and 60-second cooldown rate-limiting.
* **Official PDF Receipts:** Generates styled, university-certified receipts using OpenPDF with metadata, student info, breakdown table, and verification stamp.
* **Executive Financial Analytics:** Daily and monthly revenue aggregations, faculty breakdowns, and payment method volume reports.
* **Auditing & Monitoring:** Audit logs tracking every payment transition with actor user, role, and timestamp, alongside live JVM memory and database health endpoints.

---

## 🛠️ Technology Stack

| Component | Technology | Version |
|:---|:---|:---|
| **Runtime** | Java Development Kit (JDK) | 21 LTS |
| **Framework** | Spring Boot | 3.5.0 |
| **Persistence** | Spring Data JPA / Hibernate | 6.x |
| **Database** | PostgreSQL / H2 (Testing) | 14+ / 18 |
| **Security** | Spring Security & JJWT | 0.12.6 |
| **PDF Generation** | OpenPDF (LibrePDF) | 2.0.3 |
| **API Documentation** | Springdoc OpenAPI (Swagger UI) | 2.8.5 |
| **Testing** | JUnit 5, Mockito, Spring Test MockMvc | - |
| **Build Tool** | Apache Maven Wrapper | 3.9+ |

---

## 📋 Default Credentials (Pre-Seeded Data)

The system automatically initializes default accounts for development and testing:

| Role | Username | Password | Email | Purpose |
|:---|:---|:---|:---|:---|
| **Administrator** | `admin` | `admin123` | `admin@rupp.edu.kh` | Full system control, user CRUD, academic structures, monitoring |
| **Finance Officer** | `finance` | `finance123` | `finance@rupp.edu.kh` | Payment approvals, status transitions, financial reports |
| **Student (Dara)** | `dara_nhim` | `student123` | `dara@rupp.edu.kh` | Computer Science student (CS-01), Year 2024-2025 |
| **Student (Ayutaya)** | `ayutaya` | `student123` | `ayutaya@rupp.edu.kh` | Computer Science student (CS-01), Year 2024-2025 |
| **Student (Leangkhai)** | `leangkhai` | `student123` | `leangkhai@rupp.edu.kh` | Telecommunication Engineering student (TE-01) |
| **Student (Default)** | `student1` | `student123` | `student1@rupp.edu.kh` | Quick testing account |

---

## 🔧 Installation & Getting Started

### Prerequisites
* **Java 21 LTS** installed (`java -version`)
* **PostgreSQL 14+** running locally (or use H2 fallback)
* **Git** installed

### 1. Clone the Repository
```bash
git clone https://github.com/nhimdara/RUPP_Payment_Systerm_Backend.git
cd RUPP_Payment_Systerm_Backend
```

### 2. Configure PostgreSQL Database
Create a PostgreSQL database named `rupp_payment_db`:
```sql
CREATE DATABASE rupp_payment_db;
```

Update your connection credentials in `src/main/resources/application.yml` (or set environment variables):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/rupp_payment_db
    username: postgres
    password: your_postgres_password
```

*(Note: In automated tests, the application automatically uses an in-memory H2 database).*

### 3. Build and Run the Server
Using the provided Maven Wrapper:

**Windows (PowerShell / CMD):**
```powershell
.\mvnw.cmd clean spring-boot:run
```

**macOS / Linux:**
```bash
./mvnw clean spring-boot:run
```

The application will start on **port `8082`**:
```text
http://localhost:8082
```

---

## 🧪 Running Tests

Execute the comprehensive test suite (132 tests):
```powershell
.\mvnw.cmd test
```

### Test Suite Breakdown:
* **AllApisIntegrationTest:** 32 E2E integration tests validating all HTTP endpoints.
* **Payment Status State Machine Tests:** 28 unit tests testing state lifecycle transitions.
* **PaymentVerificationServiceTest:** 17 tests testing OTP challenge, verification, and lockouts.
* **DuplicatePaymentGuardTest & IdempotencyTest:** 11 tests verifying double-charge prevention.
* **Service Unit Tests:** Auth, User, Major, Report, and Payment confirmation tests.

```text
[INFO] Results:
[INFO] Tests run: 132, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 📖 API Documentation & Integration Files

| Resource | Location |
|:---|:---|
| **Interactive Swagger UI** | [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html) |
| **OpenAPI Specification JSON** | [http://localhost:8082/v3/api-docs](http://localhost:8082/v3/api-docs) |
| **Postman Collection** | [`postman/RUPP_Payment_System.postman_collection.json`](file:///c:/Users/darac/OneDrive/Desktop/Moblie%20Final%20Project/RUPP_Payment_Systerm_Backend/postman/RUPP_Payment_System.postman_collection.json) |
| **Postman Environment** | [`postman/RUPP_Payment_System.postman_environment.json`](file:///c:/Users/darac/OneDrive/Desktop/Moblie%20Final%20Project/RUPP_Payment_Systerm_Backend/postman/RUPP_Payment_System.postman_environment.json) |
| **Frontend & Mobile Word Guide** | [`RUPP_Payment_System_Frontend_Mobile_API_Integration_Guide.docx`](file:///c:/Users/darac/OneDrive/Desktop/Moblie%20Final%20Project/RUPP_Payment_Systerm_Backend/RUPP_Payment_System_Frontend_Mobile_API_Integration_Guide.docx) |
| **Frontend & Mobile Markdown Guide** | [`API_INTEGRATION_GUIDE.md`](file:///c:/Users/darac/OneDrive/Desktop/Moblie%20Final%20Project/RUPP_Payment_Systerm_Backend/API_INTEGRATION_GUIDE.md) |
| **Database Schema DDL** | [`db/schema.sql`](file:///c:/Users/darac/OneDrive/Desktop/Moblie%20Final%20Project/RUPP_Payment_Systerm_Backend/db/schema.sql) |
| **Database Seed Script** | [`db/seed_data.sql`](file:///c:/Users/darac/OneDrive/Desktop/Moblie%20Final%20Project/RUPP_Payment_Systerm_Backend/db/seed_data.sql) |

---

## 📡 Key API Endpoints Summary

### Authentication (`/api/v1/auth`)
* `POST /login` — Authenticate and receive JWT access & refresh tokens
* `POST /register` — Register a new student account
* `POST /refresh` — Renew access token with refresh token
* `GET /me` — Retrieve authenticated user profile

### Tuition Fees & Calculation (`/api/v1/tuition-fees`)
* `GET /calculate` — **(Student Mobile)** Calculate current student's tuition balance, remaining debt, and status
* `GET /calculate/{studentId}` — **(Staff)** Calculate tuition ledger for any student
* `GET /my-fee` — View applicable tuition fee structure
* `GET /` & `POST /` & `PUT /{id}` & `DELETE /{id}` — Manage tuition fee structures

### Payments & Lifecycle (`/api/v1/payments`)
* `POST /` — Student submits tuition fee payment (supports `Idempotency-Key`)
* `GET /my-payments` — Paginated history of student's own payments
* `GET /search` — Multi-param search with dynamic filters, pagination, and sorting
* `GET /{id}` — Retrieve payment details
* `PATCH /{id}/status` — Finance Staff transitions status (`PENDING` -> `VERIFIED` -> `CONFIRMED`)
* `GET /{id}/receipt` — View receipt metadata for confirmed payment
* `GET /{id}/receipt/pdf` — **Export official PDF receipt**

### Two-Step OTP Verification (`/api/v1/payments`)
* `POST /{paymentId}/verification/otp` — Request 6-digit OTP challenge (valid 5 min)
* `POST /verification/otp/{challengeId}/verify` — Submit code to verify payment
* `POST /verification/otp/{challengeId}/resend` — Resend code (60-second cooldown)

### Receipts & PDF Export (`/api/v1/receipts`)
* `GET /{id}/pdf` — Export receipt as binary PDF (`application/pdf`)
* `GET /payment/{paymentId}/pdf` — Export receipt PDF by payment ID
* `GET /{id}` & `GET /payment/{paymentId}` — View receipt details

### Financial Analytics (`/api/v1/reports`)
* `GET /financial-summary` — High-level KPI summary, total revenue, today/monthly revenue
* `GET /daily-revenue` — Daily revenue aggregates (filterable by date range)
* `GET /monthly-revenue` — Monthly revenue aggregates (filterable by year)
* `GET /faculty-breakdown` — Revenue breakdown by academic faculty
* `GET /payment-methods` — Payment volume breakdown by method
* `GET /payments` — Filtered payment reports

---

## 🏛️ Project Structure

```text
RUPP_Payment_Systerm_Backend/
├── postman/
│   ├── RUPP_Payment_System.postman_collection.json
│   └── RUPP_Payment_System.postman_environment.json
├── db/
│   ├── schema.sql
│   └── seed_data.sql
├── src/
│   ├── main/
│   │   ├── java/com/rupp/payment/
│   │   │   ├── config/             # SecurityConfig, DataInitializer, Swagger OpenApiConfig
│   │   │   ├── controller/         # REST Controllers (Auth, Student, Payment, Reports, Receipts)
│   │   │   ├── dto/                # Request & Response Data Transfer Objects
│   │   │   ├── entity/             # JPA Entities (User, Student, Payment, Receipt, AuditLog)
│   │   │   ├── exception/          # GlobalExceptionHandler (@RestControllerAdvice)
│   │   │   ├── repository/         # Spring Data JPA Repositories
│   │   │   ├── security/           # JWT Provider, Auth Filter, Idempotency Guard
│   │   │   ├── service/            # Core Business Services, State Machine, PDF Engine
│   │   │   └── util/               # Pagination and formatting utilities
│   │   └── resources/
│   │       ├── application.yml     # Application configuration
│   │       ├── schema.sql          # DDL tables and performance indexes
│   │       └── data.sql            # Seed data
│   └── test/
│       └── java/com/rupp/payment/  # 132 Unit & Integration Tests (100% Pass)
├── API_INTEGRATION_GUIDE.md        # Complete API reference for Frontend & Mobile
├── RUPP_Payment_System_Frontend_Mobile_API_Integration_Guide.docx  # Word document for developers
├── pom.xml                         # Maven dependencies & build settings
└── README.md                       # Project documentation
```

---

## 📄 License
This project is developed as part of the Year 3 Mobile & Web Final Exam Project at the Royal University of Phnom Penh (RUPP).  
Copyright © 2026 RUPP Payment System Group. All rights reserved.
