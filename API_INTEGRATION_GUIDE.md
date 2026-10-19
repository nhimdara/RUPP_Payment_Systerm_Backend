# Royal University of Phnom Penh (RUPP)
## Department of Computer Science — Mobile & Web Final Project
### RUPP Tuition Payment & Billing System — Frontend & Mobile API Integration Guide

**Document File (.docx):** `RUPP_Payment_System_Frontend_Mobile_API_Integration_Guide.docx`  
**Base URL (Local Development):** `http://localhost:8082`  
**Swagger / OpenAPI UI:** `http://localhost:8082/swagger-ui.html`  
**API Docs JSON:** `http://localhost:8082/v3/api-docs`  
**Postman Collection:** `postman/RUPP_Payment_System.postman_collection.json`  
**Postman Environment:** `postman/RUPP_Payment_System.postman_environment.json`  

---

## 1. System Architecture & Standard Response Envelope

All API endpoints return JSON wrapped inside a consistent `ApiResponse<T>` envelope:

```json
{
  "success": true,
  "message": "Tuition fees fetched successfully",
  "data": { ... },
  "errors": null,
  "timestamp": "2026-10-19T23:25:00"
}
```

### Standard Status Codes
* **200 OK**: Request succeeded with data.
* **201 Created**: New resource created (payment submitted, user registered, fee created).
* **400 Bad Request**: Validation failure or business logic error. Inspect `errors` map or `message`.
* **401 Unauthorized**: Missing, expired, or invalid JWT bearer token.
* **403 Forbidden**: Insufficient role privileges for target endpoint.
* **404 Not Found**: Entity ID not found in database.
* **409 Conflict**: Duplicate entry (duplicate username, fee structure, or payment transaction reference).

---

## 2. Authentication & Authorization Flow

Protected endpoints require the standard HTTP Bearer token:
```http
Authorization: Bearer <jwt_access_token>
```

### 2.1 Login
* **Method & Path:** `POST /api/v1/auth/login`
* **Access:** Public
* **Request Body:**
```json
{
  "usernameOrEmail": "ayutaya",
  "password": "student123"
}
```
* **Response (200 OK):**
```json
{
  "success": true,
  "message": "Authentication successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 5,
      "username": "ayutaya",
      "email": "ayutaya@rupp.edu.kh",
      "role": "ROLE_STUDENT"
    }
  }
}
```

### 2.2 Refresh Token
* **Method & Path:** `POST /api/v1/auth/refresh`
* **Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 2.3 Get Current Authenticated User
* **Method & Path:** `GET /api/v1/auth/me`
* **Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 5,
    "username": "ayutaya",
    "email": "ayutaya@rupp.edu.kh",
    "role": "ROLE_STUDENT"
  }
}
```

---

## 3. Student Profile & Academic Selection

### 3.1 Get Current Student Profile
* **Method & Path:** `GET /api/v1/students/my-profile`
* **Headers:** `Authorization: Bearer <student_token>`
* **Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "userId": 5,
    "username": "ayutaya",
    "email": "ayutaya@rupp.edu.kh",
    "firstName": "Ayutaya",
    "lastName": "Tab",
    "facultyId": 1,
    "facultyName": "Computer Science",
    "academicYearId": 2,
    "academicYearLabel": "2024-2025",
    "majorId": 1,
    "majorName": "Computer Science",
    "majorCode": "CS-01"
  }
}
```

### 3.2 Update Student Academic Info
* **Method & Path:** `PUT /api/v1/students/my-academic-info`
* **Request Body:**
```json
{
  "facultyId": 1,
  "academicYearId": 2,
  "majorId": 1
}
```

---

## 4. Tuition Fee Calculation Engine (For Student Home Dashboard)

### 4.1 Calculate My Tuition Balance (Primary Mobile Screen)
* **Method & Path:** `GET /api/v1/tuition-fees/calculate`
* **Role:** `ROLE_STUDENT`
* **Response (200 OK):**
```json
{
  "success": true,
  "message": "Tuition calculation completed successfully",
  "data": {
    "studentId": 2,
    "studentName": "Ayutaya Tab",
    "studentEmail": "ayutaya@rupp.edu.kh",
    "facultyName": "Computer Science",
    "academicYear": "2024-2025",
    "majorName": "Computer Science",
    "totalTuitionFee": 600.00,
    "totalPaid": 600.00,
    "remainingBalance": 0.00,
    "fullyPaid": true,
    "paymentStatus": "PAID",
    "dueDate": "2024-11-30",
    "paymentHistory": [
      {
        "id": 2,
        "studentId": 2,
        "feeStructureId": 1,
        "status": "CONFIRMED",
        "paymentDate": "2024-10-16T14:15:00",
        "totalPaid": 600.00,
        "remarks": "Tuition fee full payment",
        "receipt": {
          "id": 2,
          "receiptNumber": "RUPP-REC-2024-002",
          "totalAmount": 600.00
        }
      }
    ]
  }
}
```
* **Frontend/Mobile Display Logic:**
  * If `paymentStatus == 'PAID'`: Show green check badge, hide "Pay Now" CTA.
  * If `paymentStatus == 'PARTIAL'`: Show amber badge with remaining balance.
  * If `paymentStatus == 'UNPAID'`: Show red badge with total amount due and due date.

### 4.2 Calculate Tuition by Student ID (Finance Staff & Admin)
* **Method & Path:** `GET /api/v1/tuition-fees/calculate/{studentId}`
* **Role:** `ROLE_ADMIN`, `ROLE_FINANCE_STAFF`

---

## 5. Payment Submission & Idempotency Flow

### 5.1 Submit Payment
* **Method & Path:** `POST /api/v1/payments`
* **Role:** `ROLE_STUDENT`
* **Headers:**
  * `Authorization: Bearer <student_token>`
  * `Idempotency-Key: <UUIDv4>` (Prevents duplicate charges on retries)
* **Request Body:**
```json
{
  "feeStructureId": 1,
  "amount": 600.00,
  "paymentMethod": "KHQR_BAKONG",
  "transactionRef": "TXN-KHQR-20261019-001",
  "remarks": "Semester 1 payment via Bakong app"
}
```
* **Supported Payment Methods:** `KHQR_BAKONG`, `ABA_PAYWAY`, `WING`, `ACLEDAMOBILE`, `CASH`.
* **Response (201 Created):**
```json
{
  "success": true,
  "message": "Payment submitted successfully",
  "data": {
    "id": 3,
    "studentId": 2,
    "studentName": "Ayutaya Tab",
    "status": "PENDING",
    "paymentDate": "2026-10-19T23:20:00",
    "totalPaid": 600.00,
    "details": [
      {
        "id": 3,
        "method": "KHQR_BAKONG",
        "transactionRef": "TXN-KHQR-20261019-001",
        "amount": 600.00
      }
    ]
  }
}
```

---

## 6. Two-Step OTP Verification Flow

For mobile security, payments can require a 6-digit one-time PIN:

1. **Request OTP Challenge:** `POST /api/v1/payments/{paymentId}/verification/otp`
   * Response returns: `challengeId` (UUID) and `expiresInSeconds` (300).
2. **Submit OTP Code:** `POST /api/v1/payments/verification/otp/{challengeId}/verify`
   * Body: `{ "code": "123456" }`
   * On success: Payment status transitions to `VERIFIED`.
3. **Resend OTP:** `POST /api/v1/payments/verification/otp/{challengeId}/resend`
   * Rate limited: 60-second cooldown between resends.

---

## 7. Official PDF Receipts

The backend provides styled, official Royal University of Phnom Penh receipts in binary PDF format (`application/pdf`).

### PDF Export Endpoints
* `GET /api/v1/receipts/{id}/pdf`: Download receipt by receipt ID.
* `GET /api/v1/receipts/payment/{paymentId}/pdf`: Download receipt by payment ID.
* `GET /api/v1/payments/{paymentId}/receipt/pdf`: Direct payment shortcut.
* **Headers Returned:**
  * `Content-Type: application/pdf`
  * `Content-Disposition: inline; filename="receipt-RUPP-REC-2024-001.pdf"`

---

## 8. Financial Dashboard & Analytics (Web Portal)

Endpoints for the Finance Staff & Admin Analytics Dashboard:

1. **Executive Financial Summary:** `GET /api/v1/reports/financial-summary`
   * Returns `totalRevenue`, `todayRevenue`, `currentMonthRevenue`, `totalPendingAmount`, `totalTransactions`, `dailyBreakdown`, and `monthlyBreakdown`.
2. **Daily Revenue Metrics:** `GET /api/v1/reports/daily-revenue?startDate=2024-10-01&endDate=2024-10-31`
   * Daily aggregate revenue & count (for Line Charts).
3. **Monthly Revenue Metrics:** `GET /api/v1/reports/monthly-revenue?year=2024`
   * Monthly aggregate revenue & count (for Bar Charts).
4. **Faculty Breakdown:** `GET /api/v1/reports/faculty-breakdown`
   * Total revenue and transactions by faculty (for Pie / Donut Charts).
5. **Payment Method Volumes:** `GET /api/v1/reports/payment-methods`
   * Total revenue and counts per payment channel (KHQR, ABA, Wing, Cash).
6. **Multi-Param Payment Search:** `GET /api/v1/payments/search`
   * Parameters: `studentId`, `facultyId`, `status`, `startDate`, `endDate`, `page`, `size`, `sortBy`, `sortDir`.

---

## 9. Mobile App Screen-to-API Mapping

| Mobile Screen | API Endpoint | UI Components & Behavior |
|:---|:---|:---|
| **1. Login Screen** | `POST /api/v1/auth/login` | Inputs for username/email & password. Stores `accessToken` in secure storage. |
| **2. Academic Selection** | `GET /faculties`, `GET /academic-years`, `GET /majors`, `PUT /students/my-academic-info` | Onboarding screen with dropdowns. Saves student major and faculty. |
| **3. Home Dashboard** | `GET /api/v1/tuition-fees/calculate`, `GET /api/v1/auth/me` | Greeting, Tuition Balance Card (Total Fee, Paid, Balance), Status badge, "Pay Now" button. |
| **4. Payment Gateway** | `POST /api/v1/payments` | Method selection (KHQR, ABA, Wing). Generates KHQR QR code or accepts transaction slip. Sends `Idempotency-Key`. |
| **5. OTP Verification** | `POST /payments/{id}/verification/otp`, `POST /verification/otp/{challengeId}/verify` | 6-digit PIN boxes, 5-minute timer countdown, Resend OTP button. |
| **6. Receipt Viewer** | `GET /api/v1/receipts/{id}`, `GET /api/v1/receipts/{id}/pdf` | Payment confirmation card, "Download PDF Receipt" action (saves to device files or opens in-app). |
| **7. Payment History** | `GET /api/v1/payments/my-payments` | Infinite-scrolling list of past transactions with status indicators (`CONFIRMED`, `PENDING`, `REJECTED`). |

---

## 10. Pre-Seeded Accounts for Testing

| Role | Username | Password | Notes |
|:---|:---|:---|:---|
| **Administrator** | `admin` | `admin123` | Full administrative rights over users, academic structures, and monitoring. |
| **Finance Officer** | `finance` | `finance123` | Can approve/reject payments, view financial reports, and inspect ledger. |
| **Student (Dara)** | `dara_nhim` | `student123` | Enrolled in Computer Science, Year 2024-2025. |
| **Student (Ayutaya)** | `ayutaya` | `student123` | Enrolled in Computer Science, Year 2024-2025. |
| **Student (Leangkhai)** | `leangkhai` | `student123` | Enrolled in Telecommunication Engineering. |
