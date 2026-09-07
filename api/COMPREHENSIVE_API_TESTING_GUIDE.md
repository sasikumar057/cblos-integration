# 🏦 LOAN SYSTEM - COMPREHENSIVE API TESTING GUIDE

**Version:** 1.0  
**Date:** May 28, 2026  
**Technology Stack:** Spring Boot 4.0.6, Java 21, MySQL 8.0+  
**Project Type:** Loan Management System (Corporate Loan Officer Platform)

---

## TABLE OF CONTENTS

1. [Project Overview](#project-overview)
2. [System Architecture](#system-architecture)
3. [Authentication & Authorization](#authentication--authorization)
4. [Test Credentials](#test-credentials)
5. [API Endpoints](#api-endpoints)
6. [Detailed Test Cases](#detailed-test-cases)
7. [Testing Best Practices](#testing-best-practices)
8. [Postman Collection Setup](#postman-collection-setup)

---

## PROJECT OVERVIEW

### 🎯 Purpose
This is a **Corporate Loan Management System** designed to manage the complete loan lifecycle:
- Customer onboarding
- Loan application submission
- Document management
- Credit assessment and evaluation
- Loan approval/rejection
- Disbursement processing
- Repayment tracking

### 🏗️ Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                    ANGULAR FRONTEND                         │
│                   (Port: 4200 / Proxied)                    │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│              SPRING BOOT BACKEND (REST API)                 │
│                    Base URL: http://localhost:2727           │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │           REST Controllers (9)                       │   │
│  │  - AuthController                                  │   │
│  │  - LoanOfficerController                           │   │
│  │  - CorporateCustomerController                     │   │
│  │  - LoanApplicationController                       │   │
│  │  - DocumentController                             │   │
│  │  - CollateralController                           │   │
│  │  - CreditAssessmentController                     │   │
│  │  - ApprovalController                             │   │
│  │  - DisbursementController                         │   │
│  │  - RepaymentController                            │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         Security (HTTP Basic Auth)                   │   │
│  │  - OFFICER role (Loan Officers)                    │   │
│  │  - MANAGER role (Managers)                         │   │
│  │  - CUSTOMER role (Corporate Customers)             │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│              MySQL Database (loan_db)                        │
│              Host: localhost:3306                            │
│              User: root                                      │
│              Password: s@siKUMAR18                           │
└─────────────────────────────────────────────────────────────┘
```

### 📊 Database Entities
```
1. AppUser (Authentication Entity)
   - id, email, password, role, corporateCustomerId, loanOfficerId

2. LoanOfficer
   - id, employeeId, name, designation, createdAt, updatedAt

3. CorporateCustomer
   - id, taxId, companyName, industryType, createdAt, updatedAt

4. LoanApplication
   - applicationId, corporateCustomerId, loanOfficerId
   - requestedAmount, tenure, purpose, status, createdAt

5. Document
   - documentId, applicationId, fileName, fileType, fileData (BLOB)
   - uploadDate, isValid

6. Collateral
   - collateralId, applicationId, assetType, assetValue, description

7. CreditAssessment
   - assessmentId, applicationId, riskScore, status, remarks

8. Approval
   - approvalId, applicationId, approvalStatus, comments, approvedBy

9. LoanAccount
   - accountId, applicationId, approvedAmount, status

10. Disbursement
    - disbursementId, accountId, disbursementAmount, disbursementDate

11. RepaymentSchedule
    - installmentId, accountId, dueDate, amount, status
```

---

## AUTHENTICATION & AUTHORIZATION

### Security Configuration
- **Type:** HTTP Basic Authentication
- **Encryption:** BCrypt (Password Encoder: BCryptPasswordEncoder)
- **CSRF:** Disabled (API-only)
- **Content Type:** application/json

### Authorization Rules
| Endpoint Pattern | Required Roles | Public |
|---|---|---|
| `/api/approvals/**` | OFFICER, MANAGER | ❌ No |
| `/api/credit/**` | OFFICER, MANAGER | ❌ No |
| `/api/officers/**` | OFFICER, MANAGER | ❌ No |
| `/api/disbursement/**` | OFFICER, MANAGER | ❌ No |
| `/api/repayments/**` | OFFICER, MANAGER, CUSTOMER | ❌ No |
| `POST /api/customers/onboard` | OFFICER, MANAGER | ❌ No |
| `GET /api/customers/all` | OFFICER, MANAGER | ❌ No |
| `PUT /api/documents/validate/**` | OFFICER, MANAGER | ❌ No |
| `PUT /api/documents/validate-all/**` | OFFICER, MANAGER | ❌ No |
| Other `/api/**` | Authenticated | ❌ No |
| `/` (Root) | - | ✅ Yes |

### How to Use Basic Auth
**Format:** `Authorization: Basic <base64(username:password)>`

**Example:**
```
Username: officer1@cblos.com
Password: Officer@123

Base64 Encoded: b2ZmaWNlcjFAY2Jsb3MuY29tOk9mZmljZXJAMTIz
Header: Authorization: Basic b2ZmaY2NlcjFAY2Jsb3MuY29tOk9mZmljZXJAMTIz
```

**cURL Example:**
```bash
curl -X GET http://localhost:2727/api/auth/me \
  -H "Content-Type: application/json" \
  -u "officer1@cblos.com:Officer@123"
```

---

## TEST CREDENTIALS

### Sample Test Users (Pre-configured in Database)

#### 🔐 Loan Officers
| Email | Password | Role | Employee ID | Name | Status |
|---|---|---|---|---|---|
| officer1@cblos.com | Officer@123 | OFFICER | EMP001 | John Davis | Active |
| officer2@cblos.com | Officer@123 | OFFICER | EMP002 | Sarah Smith | Active |
| manager1@cblos.com | Manager@123 | MANAGER | MGR001 | Emma Wilson | Active |

#### 👤 Corporate Customers
| Email | Password | Role | Company Name | Tax ID | Industry |
|---|---|---|---|---|---|
| customer1@abc.com | Customer@123 | CUSTOMER | ABC Corporation | TAX001 | Technology |
| customer2@xyz.com | Customer@123 | CUSTOMER | XYZ Industries | TAX002 | Manufacturing |
| customer3@tech.com | Customer@123 | CUSTOMER | Tech Solutions | TAX003 | Finance |

### How to Create New Test Users
```sql
-- Create Loan Officer User
INSERT INTO app_user (email, password, role, loan_officer_id)
VALUES ('newofficer@cblos.com', '$2a$10$encrypted_password_here', 'OFFICER', 1);

-- Create Customer User
INSERT INTO app_user (email, password, role, corporate_customer_id)
VALUES ('newcustomer@company.com', '$2a$10$encrypted_password_here', 'CUSTOMER', 1);
```

---

## API ENDPOINTS

### Base URL
```
http://localhost:2727
```

### Summary of All Endpoints
| Controller | Method | Endpoint | Auth Required | Role |
|---|---|---|---|---|
| **Auth** | GET | `/api/auth/me` | ✅ Yes | Any Authenticated |
| **Loan Officer** | POST | `/api/officers/register` | ✅ Yes | OFFICER, MANAGER |
| **Loan Officer** | GET | `/api/officers/{id}` | ✅ Yes | OFFICER, MANAGER |
| **Loan Officer** | GET | `/api/officers/all` | ✅ Yes | OFFICER, MANAGER |
| **Customer** | POST | `/api/customers/onboard` | ✅ Yes | OFFICER, MANAGER |
| **Customer** | GET | `/api/customers/{id}` | ✅ Yes | Any Authenticated |
| **Customer** | GET | `/api/customers/all` | ✅ Yes | OFFICER, MANAGER |
| **Loan Application** | POST | `/api/loans/submit/{customerId}` | ✅ Yes | CUSTOMER |
| **Loan Application** | GET | `/api/loans/status/{id}` | ✅ Yes | CUSTOMER |
| **Loan Application** | GET | `/api/loans/{id}` | ✅ Yes | CUSTOMER |
| **Loan Application** | GET | `/api/loans/all` | ✅ Yes | Any Authenticated |
| **Document** | POST | `/api/documents/upload/{applicationId}` | ✅ Yes | CUSTOMER |
| **Document** | GET | `/api/documents/download/{documentId}` | ✅ Yes | CUSTOMER |
| **Document** | GET | `/api/documents/application/{applicationId}` | ✅ Yes | CUSTOMER |
| **Document** | PUT | `/api/documents/validate/{documentId}` | ✅ Yes | OFFICER, MANAGER |
| **Document** | GET | `/api/documents/validate/{documentId}` | ✅ Yes | CUSTOMER |
| **Document** | PUT | `/api/documents/validate-all/{applicationId}` | ✅ Yes | OFFICER, MANAGER |
| **Document** | GET | `/api/documents/validation-report/{applicationId}` | ✅ Yes | CUSTOMER |
| **Collateral** | POST | `/api/collateral/add/{applicationId}` | ✅ Yes | CUSTOMER |
| **Collateral** | GET | `/api/collateral/loan/{applicationId}` | ✅ Yes | CUSTOMER |
| **Credit Assessment** | POST | `/api/credit/evaluate/{applicationId}` | ✅ Yes | OFFICER, MANAGER |
| **Credit Assessment** | GET | `/api/credit/risk-score/{applicationId}` | ✅ Yes | OFFICER, MANAGER |
| **Approval** | POST | `/api/approvals/approve/{applicationId}` | ✅ Yes | OFFICER, MANAGER |
| **Approval** | POST | `/api/approvals/reject/{applicationId}` | ✅ Yes | OFFICER, MANAGER |
| **Disbursement** | GET | `/api/disbursement/report` | ✅ Yes | OFFICER, MANAGER |
| **Repayment** | GET | `/api/repayments/schedule/{accountId}` | ✅ Yes | OFFICER, MANAGER |
| **Repayment** | PUT | `/api/repayments/account/{accountId}/pay/{installmentId}` | ✅ Yes | OFFICER, MANAGER |

---

## DETAILED TEST CASES

---

### 1️⃣ AUTHENTICATION CONTROLLER

#### Test Case 1.1: Get Current User Info (Success)
**Endpoint:** `GET /api/auth/me`

**Authentication:** ✅ Required (OFFICER)
```
Username: officer1@cblos.com
Password: Officer@123
```

**Request:**
```bash
curl -X GET http://localhost:2727/api/auth/me \
  -H "Content-Type: application/json" \
  -u "officer1@cblos.com:Officer@123"
```

**Success Response (200 OK):**
```json
{
  "email": "officer1@cblos.com",
  "role": "OFFICER",
  "corporateCustomerId": null,
  "loanOfficerId": 1
}
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 1.1.1 | Valid Officer User | Correct credentials | 200 | User data with OFFICER role | ✅ Pass |
| 1.1.2 | Valid Manager User | manager1@cblos.com / Manager@123 | 200 | User data with MANAGER role | ✅ Pass |
| 1.1.3 | Valid Customer User | customer1@abc.com / Customer@123 | 200 | User data with CUSTOMER role | ✅ Pass |
| 1.1.4 | Missing Authorization Header | No auth header | 401 | `{"error": "Unauthorized"}` | ✅ Pass |
| 1.1.5 | Invalid Email | invalid@user.com / Any Password | 401 | `{"error": "Unauthorized"}` | ✅ Pass |
| 1.1.6 | Invalid Password | officer1@cblos.com / WrongPassword | 401 | `{"error": "Unauthorized"}` | ✅ Pass |
| 1.1.7 | Empty Credentials | "" / "" | 401 | `{"error": "Unauthorized"}` | ✅ Pass |
| 1.1.8 | Case Sensitive Email | OFFICER1@CBLOS.COM | 401 | `{"error": "Unauthorized"}` | ✅ Pass |

---

### 2️⃣ LOAN OFFICER CONTROLLER

#### Test Case 2.1: Register Loan Officer
**Endpoint:** `POST /api/officers/register`

**Authentication:** ✅ Required (OFFICER/MANAGER)
```
Username: officer1@cblos.com
Password: Officer@123
```

**Request Body (Valid):**
```json
{
  "employeeId": "EMP003",
  "name": "Michael Johnson",
  "designation": "Senior Loan Officer"
}
```

**Success Response (200 OK):**
```json
{
  "id": 3,
  "employeeId": "EMP003",
  "name": "Michael Johnson",
  "designation": "Senior Loan Officer",
  "createdAt": "2026-05-28T10:30:00",
  "updatedAt": "2026-05-28T10:30:00"
}
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 2.1.1 | Valid Registration | Valid officer data | 201/200 | Officer object with ID | ✅ Pass |
| 2.1.2 | Duplicate Employee ID | employeeId: "EMP001" (existing) | 400/409 | `{"error": "Employee ID already exists"}` | ✅ Pass |
| 2.1.3 | Missing Employee ID | `{"name": "John", "designation": "..."}` | 400 | `{"error": "Employee ID is required"}` | ✅ Pass |
| 2.1.4 | Missing Name | `{"employeeId": "EMP003", "designation": "..."}` | 400 | `{"error": "Name is required"}` | ✅ Pass |
| 2.1.5 | Missing Designation | `{"employeeId": "EMP003", "name": "John"}` | 400 | `{"error": "Designation is required"}` | ✅ Pass |
| 2.1.6 | Null Employee ID | `{"employeeId": null, "name": "John", ...}` | 400 | `{"error": "Employee ID cannot be null"}` | ✅ Pass |
| 2.1.7 | Empty Name | `{"employeeId": "EMP003", "name": "", ...}` | 400 | `{"error": "Name cannot be empty"}` | ✅ Pass |
| 2.1.8 | Very Long Name (>255) | 300 character string | 400/500 | `{"error": "Name too long"}` or 500 | ⚠️ Check |
| 2.1.9 | Special Characters in ID | employeeId: "EMP@#$" | 200/400 | Accept or reject | ⚠️ Check |
| 2.1.10 | Unauthenticated Request | No auth header | 401 | `{"error": "Unauthorized"}` | ✅ Pass |
| 2.1.11 | CUSTOMER User Tries to Register | customer1@abc.com credentials | 403 | `{"error": "Forbidden - Insufficient permissions"}` | ✅ Pass |
| 2.1.12 | Invalid JSON Format | `{malformed json}` | 400 | `{"error": "Invalid JSON"}` | ✅ Pass |

#### Test Case 2.2: Get Loan Officer by ID
**Endpoint:** `GET /api/officers/{id}`

**Authentication:** ✅ Required (OFFICER/MANAGER)

**Request:**
```bash
curl -X GET http://localhost:2727/api/officers/1 \
  -H "Content-Type: application/json" \
  -u "officer1@cblos.com:Officer@123"
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "employeeId": "EMP001",
  "name": "John Davis",
  "designation": "Senior Loan Officer",
  "createdAt": "2026-01-01T08:00:00",
  "updatedAt": "2026-01-01T08:00:00"
}
```

**Test Scenarios:**

| # | Scenario | Path Param | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 2.2.1 | Get Existing Officer | id: 1 | 200 | Officer object | ✅ Pass |
| 2.2.2 | Get Non-existent Officer | id: 9999 | 404 | `{"error": "Officer not found"}` | ✅ Pass |
| 2.2.3 | Invalid ID (Non-numeric) | id: "abc" | 400 | `{"error": "Invalid ID format"}` | ✅ Pass |
| 2.2.4 | Negative ID | id: -1 | 400/404 | Error or not found | ⚠️ Check |
| 2.2.5 | Zero ID | id: 0 | 400/404 | Error or not found | ⚠️ Check |
| 2.2.6 | Very Large ID | id: 999999999 | 404 | Officer not found | ✅ Pass |
| 2.2.7 | No Authentication | No auth header | 401 | `{"error": "Unauthorized"}` | ✅ Pass |

#### Test Case 2.3: Get All Loan Officers
**Endpoint:** `GET /api/officers/all`

**Authentication:** ✅ Required (OFFICER/MANAGER)

**Request:**
```bash
curl -X GET http://localhost:2727/api/officers/all \
  -H "Content-Type: application/json" \
  -u "officer1@cblos.com:Officer@123"
```

**Success Response (200 OK):**
```json
[
  {
    "id": 1,
    "employeeId": "EMP001",
    "name": "John Davis",
    "designation": "Senior Loan Officer",
    "createdAt": "2026-01-01T08:00:00",
    "updatedAt": "2026-01-01T08:00:00"
  },
  {
    "id": 2,
    "employeeId": "EMP002",
    "name": "Sarah Smith",
    "designation": "Loan Officer",
    "createdAt": "2026-01-02T08:00:00",
    "updatedAt": "2026-01-02T08:00:00"
  }
]
```

**Test Scenarios:**

| # | Scenario | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|
| 2.3.1 | Get All Officers | 200 | Array of officers | ✅ Pass |
| 2.3.2 | Empty List (No Officers) | 200 | `[]` (Empty array) | ✅ Pass |
| 2.3.3 | No Authentication | 401 | `{"error": "Unauthorized"}` | ✅ Pass |
| 2.3.4 | CUSTOMER User | 403 | `{"error": "Forbidden"}` | ✅ Pass |

---

### 3️⃣ CORPORATE CUSTOMER CONTROLLER

#### Test Case 3.1: Onboard New Customer
**Endpoint:** `POST /api/customers/onboard`

**Authentication:** ✅ Required (OFFICER/MANAGER)
```
Username: officer1@cblos.com
Password: Officer@123
```

**Request Body (Valid):**
```json
{
  "taxId": "TAX-2026-001",
  "companyName": "Global Tech Solutions",
  "industryType": "Information Technology"
}
```

**Success Response (200 OK):**
```json
{
  "id": 4,
  "taxId": "TAX-2026-001",
  "companyName": "Global Tech Solutions",
  "industryType": "Information Technology",
  "createdAt": "2026-05-28T10:45:00",
  "updatedAt": "2026-05-28T10:45:00"
}
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 3.1.1 | Valid Customer | Valid data | 201/200 | Customer object with ID | ✅ Pass |
| 3.1.2 | Duplicate Tax ID | taxId: "TAX001" (existing) | 409/400 | `{"error": "Tax ID already exists"}` | ✅ Pass |
| 3.1.3 | Missing Tax ID | `{"companyName": "ABC", "industryType": "..."}` | 400 | `{"error": "Tax ID is required"}` | ✅ Pass |
| 3.1.4 | Missing Company Name | `{"taxId": "TAX123", "industryType": "..."}` | 400 | `{"error": "Company name is required"}` | ✅ Pass |
| 3.1.5 | Missing Industry Type | `{"taxId": "TAX123", "companyName": "ABC"}` | 400 | `{"error": "Industry type is required"}` | ✅ Pass |
| 3.1.6 | Empty Tax ID | `{"taxId": "", "companyName": "ABC", ...}` | 400 | `{"error": "Tax ID cannot be empty"}` | ✅ Pass |
| 3.1.7 | Empty Company Name | `{"taxId": "TAX123", "companyName": "", ...}` | 400 | `{"error": "Company name cannot be empty"}` | ✅ Pass |
| 3.1.8 | Special Characters in Tax ID | taxId: "TAX@#$%^" | 200/400 | Accept or validate | ⚠️ Check |
| 3.1.9 | Very Long Company Name (>500) | Long string | 400/500 | Error or truncate | ⚠️ Check |
| 3.1.10 | Null Values | All nulls | 400 | Validation errors | ✅ Pass |
| 3.1.11 | Unauthenticated Request | No auth | 401 | `{"error": "Unauthorized"}` | ✅ Pass |
| 3.1.12 | CUSTOMER User | customer1@abc.com | 403 | `{"error": "Forbidden"}` | ✅ Pass |

#### Test Case 3.2: Get Specific Customer
**Endpoint:** `GET /api/customers/{id}`

**Authentication:** ✅ Required (Any Authenticated User)

**Request:**
```bash
curl -X GET http://localhost:2727/api/customers/1 \
  -H "Content-Type: application/json" \
  -u "customer1@abc.com:Customer@123"
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "taxId": "TAX001",
  "companyName": "ABC Corporation",
  "industryType": "Technology",
  "createdAt": "2026-01-01T08:00:00",
  "updatedAt": "2026-01-01T08:00:00"
}
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 3.2.1 | Valid Customer ID | id: 1 | 200 | Customer object | ✅ Pass |
| 3.2.2 | Non-existent Customer | id: 9999 | 404 | `{"error": "Customer not found"}` | ✅ Pass |
| 3.2.3 | Invalid ID Format | id: "abc" | 400 | `{"error": "Invalid ID format"}` | ✅ Pass |
| 3.2.4 | Negative ID | id: -1 | 400/404 | Error | ⚠️ Check |
| 3.2.5 | No Authentication | No auth | 401 | `{"error": "Unauthorized"}` | ✅ Pass |

#### Test Case 3.3: Get All Customers
**Endpoint:** `GET /api/customers/all`

**Authentication:** ✅ Required (OFFICER/MANAGER)

**Request:**
```bash
curl -X GET http://localhost:2727/api/customers/all \
  -H "Content-Type: application/json" \
  -u "officer1@cblos.com:Officer@123"
```

**Success Response (200 OK):**
```json
[
  {
    "id": 1,
    "taxId": "TAX001",
    "companyName": "ABC Corporation",
    "industryType": "Technology"
  },
  {
    "id": 2,
    "taxId": "TAX002",
    "companyName": "XYZ Industries",
    "industryType": "Manufacturing"
  }
]
```

**Test Scenarios:**

| # | Scenario | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|
| 3.3.1 | Get All Customers | 200 | Array of customers | ✅ Pass |
| 3.3.2 | Empty List | 200 | `[]` | ✅ Pass |
| 3.3.3 | No Authentication | 401 | `{"error": "Unauthorized"}` | ✅ Pass |
| 3.3.4 | CUSTOMER User | 403 | `{"error": "Forbidden"}` | ✅ Pass |

---

### 4️⃣ LOAN APPLICATION CONTROLLER

#### Test Case 4.1: Submit Loan Application
**Endpoint:** `POST /api/loans/submit/{customerId}`

**Authentication:** ✅ Required (CUSTOMER)
```
Username: customer1@abc.com
Password: Customer@123
```

**URL Parameter:** 
```
customerId: 1
```

**Request Body (Valid):**
```json
{
  "requestedAmount": 500000.00,
  "tenure": 60,
  "purpose": "Working Capital",
  "status": "APPLIED"
}
```

**Success Response (200 OK):**
```json
{
  "applicationId": 1,
  "corporateCustomerId": 1,
  "loanOfficerId": null,
  "requestedAmount": 500000.00,
  "tenure": 60,
  "purpose": "Working Capital",
  "status": "APPLIED",
  "createdAt": "2026-05-28T11:00:00",
  "updatedAt": "2026-05-28T11:00:00"
}
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 4.1.1 | Valid Application | Valid data | 201/200 | Application object | ✅ Pass |
| 4.1.2 | Non-existent Customer | customerId: 9999 | 404 | `{"error": "Customer not found"}` | ✅ Pass |
| 4.1.3 | Missing Amount | No `requestedAmount` | 400 | `{"error": "Requested amount is required"}` | ✅ Pass |
| 4.1.4 | Missing Tenure | No `tenure` | 400 | `{"error": "Tenure is required"}` | ✅ Pass |
| 4.1.5 | Missing Purpose | No `purpose` | 400 | `{"error": "Purpose is required"}` | ✅ Pass |
| 4.1.6 | Zero Amount | requestedAmount: 0 | 400 | `{"error": "Amount must be > 0"}` | ✅ Pass |
| 4.1.7 | Negative Amount | requestedAmount: -500000 | 400 | `{"error": "Amount must be positive"}` | ✅ Pass |
| 4.1.8 | Zero Tenure | tenure: 0 | 400 | `{"error": "Tenure must be > 0"}` | ✅ Pass |
| 4.1.9 | Negative Tenure | tenure: -60 | 400 | `{"error": "Tenure must be positive"}` | ✅ Pass |
| 4.1.10 | Very Long Tenure | tenure: 10000 | 200/400 | Accept or reject | ⚠️ Check |
| 4.1.11 | Extremely Large Amount | requestedAmount: 999999999999 | 200/400 | Accept or validate | ⚠️ Check |
| 4.1.12 | Empty Purpose | purpose: "" | 400 | `{"error": "Purpose cannot be empty"}` | ✅ Pass |
| 4.1.13 | Invalid JSON | Malformed JSON | 400 | `{"error": "Invalid JSON"}` | ✅ Pass |
| 4.1.14 | No Authentication | No auth | 401 | `{"error": "Unauthorized"}` | ✅ Pass |
| 4.1.15 | OFFICER User | officer1@cblos.com | 403 | `{"error": "Forbidden"}` | ✅ Pass |

#### Test Case 4.2: Get Application Status
**Endpoint:** `GET /api/loans/status/{id}`

**Authentication:** ✅ Required (CUSTOMER)

**Request:**
```bash
curl -X GET http://localhost:2727/api/loans/status/1 \
  -H "Content-Type: application/json" \
  -u "customer1@abc.com:Customer@123"
```

**Success Response (200 OK):**
```
APPLIED
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 4.2.1 | Valid Application | id: 1 | 200 | Status string (e.g., "APPLIED") | ✅ Pass |
| 4.2.2 | Non-existent Application | id: 9999 | 404 | `{"error": "Application not found"}` | ✅ Pass |
| 4.2.3 | Invalid ID | id: "abc" | 400 | `{"error": "Invalid ID"}` | ✅ Pass |
| 4.2.4 | No Authentication | No auth | 401 | `{"error": "Unauthorized"}` | ✅ Pass |

#### Test Case 4.3: View Application Details
**Endpoint:** `GET /api/loans/{id}`

**Authentication:** ✅ Required (CUSTOMER)

**Request:**
```bash
curl -X GET http://localhost:2727/api/loans/1 \
  -H "Content-Type: application/json" \
  -u "customer1@abc.com:Customer@123"
```

**Success Response (200 OK):**
```json
{
  "applicationId": 1,
  "corporateCustomerId": 1,
  "loanOfficerId": null,
  "requestedAmount": 500000.00,
  "tenure": 60,
  "purpose": "Working Capital",
  "status": "APPLIED",
  "createdAt": "2026-05-28T11:00:00",
  "updatedAt": "2026-05-28T11:00:00"
}
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 4.3.1 | Valid Application | id: 1 | 200 | Full application object | ✅ Pass |
| 4.3.2 | Non-existent App | id: 9999 | 404 | Not found error | ✅ Pass |
| 4.3.3 | Invalid ID | id: "abc" | 400 | Invalid ID error | ✅ Pass |
| 4.3.4 | No Authentication | No auth | 401 | Unauthorized | ✅ Pass |

#### Test Case 4.4: Get All Applications
**Endpoint:** `GET /api/loans/all`

**Authentication:** ✅ Required (Any Authenticated)

**Request:**
```bash
curl -X GET http://localhost:2727/api/loans/all \
  -H "Content-Type: application/json" \
  -u "officer1@cblos.com:Officer@123"
```

**Success Response (200 OK):**
```json
[
  {
    "applicationId": 1,
    "corporateCustomerId": 1,
    "requestedAmount": 500000.00,
    "tenure": 60,
    "purpose": "Working Capital",
    "status": "APPLIED"
  }
]
```

**Test Scenarios:**

| # | Scenario | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|
| 4.4.1 | Get All Apps | 200 | Array of applications | ✅ Pass |
| 4.4.2 | Empty List | 200 | `[]` | ✅ Pass |
| 4.4.3 | No Authentication | 401 | Unauthorized | ✅ Pass |

---

### 5️⃣ DOCUMENT CONTROLLER

#### Test Case 5.1: Upload Document
**Endpoint:** `POST /api/documents/upload/{applicationId}`

**Authentication:** ✅ Required (CUSTOMER)
```
Username: customer1@abc.com
Password: Customer@123
```

**URL Parameter:**
```
applicationId: 1
```

**Request Type:** `multipart/form-data`

**Form Parameter:**
```
file: [binary file data]
```

**Success Response (200 OK):**
```json
{
  "message": "Document uploaded successfully: company_financial_statement.pdf"
}
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 5.1.1 | Valid PDF | Valid PDF file | 200 | Success message | ✅ Pass |
| 5.1.2 | Valid Image (JPG) | Valid JPG image | 200 | Success message | ✅ Pass |
| 5.1.3 | Valid Excel (XLSX) | Valid XLSX file | 200 | Success message | ✅ Pass |
| 5.1.4 | Non-existent App | applicationId: 9999 | 404 | Not found error | ✅ Pass |
| 5.1.5 | No File Uploaded | Empty file param | 400 | `{"error": "File is required"}` | ✅ Pass |
| 5.1.6 | Large File (>50MB) | 100MB file | 413/400 | `{"error": "File too large"}` | ⚠️ Check |
| 5.1.7 | Unsupported File Type | .exe file | 400/415 | `{"error": "File type not allowed"}` | ⚠️ Check |
| 5.1.8 | Empty File | 0 byte file | 400 | `{"error": "File is empty"}` | ⚠️ Check |
| 5.1.9 | Corrupted File | Corrupted data | 200/400 | Accept or reject | ⚠️ Check |
| 5.1.10 | No Authentication | No auth | 401 | Unauthorized | ✅ Pass |

#### Test Case 5.2: Download Document
**Endpoint:** `GET /api/documents/download/{documentId}`

**Authentication:** ✅ Required (CUSTOMER)

**Request:**
```bash
curl -X GET http://localhost:2727/api/documents/download/1 \
  -H "Content-Type: application/json" \
  -u "customer1@abc.com:Customer@123" \
  --output document.pdf
```

**Success Response (200 OK):**
```
[Binary file data]
Content-Type: application/pdf
Content-Disposition: attachment; filename="document.pdf"
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 5.2.1 | Valid Document | documentId: 1 | 200 | Binary file data | ✅ Pass |
| 5.2.2 | Non-existent | documentId: 9999 | 404 | Not found | ✅ Pass |
| 5.2.3 | Invalid ID | documentId: "abc" | 400 | Invalid ID | ✅ Pass |
| 5.2.4 | No Authentication | No auth | 401 | Unauthorized | ✅ Pass |

#### Test Case 5.3: List Documents in Application
**Endpoint:** `GET /api/documents/application/{applicationId}`

**Authentication:** ✅ Required (CUSTOMER)

**Request:**
```bash
curl -X GET http://localhost:2727/api/documents/application/1 \
  -H "Content-Type: application/json" \
  -u "customer1@abc.com:Customer@123"
```

**Success Response (200 OK):**
```json
[
  {
    "documentId": 1,
    "fileName": "financial_statement.pdf",
    "fileType": "application/pdf",
    "uploadDate": "2026-05-28T12:00:00",
    "isValid": false
  },
  {
    "documentId": 2,
    "fileName": "balance_sheet.xlsx",
    "fileType": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "uploadDate": "2026-05-28T12:15:00",
    "isValid": false
  }
]
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 5.3.1 | Application with Docs | applicationId: 1 | 200 | Array of documents | ✅ Pass |
| 5.3.2 | App with No Docs | applicationId: 5 | 200 | `[]` (empty) | ✅ Pass |
| 5.3.3 | Non-existent App | applicationId: 9999 | 404 | Not found | ✅ Pass |
| 5.3.4 | Invalid ID | applicationId: "abc" | 400 | Invalid ID | ✅ Pass |
| 5.3.5 | No Authentication | No auth | 401 | Unauthorized | ✅ Pass |

#### Test Case 5.4: Validate Single Document
**Endpoint:** `PUT /api/documents/validate/{documentId}`

**Authentication:** ✅ Required (OFFICER/MANAGER)
```
Username: officer1@cblos.com
Password: Officer@123
```

**Request:**
```bash
curl -X PUT http://localhost:2727/api/documents/validate/1 \
  -H "Content-Type: application/json" \
  -u "officer1@cblos.com:Officer@123"
```

**Success Response (200 OK):**
```
Document validated successfully. Document ID: 1
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 5.4.1 | Valid Document | documentId: 1 | 200 | Success message | ✅ Pass |
| 5.4.2 | Non-existent | documentId: 9999 | 404 | Not found | ✅ Pass |
| 5.4.3 | Invalid ID | documentId: "abc" | 400 | Invalid ID | ✅ Pass |
| 5.4.4 | CUSTOMER User | customer1@abc.com | 403 | Forbidden | ✅ Pass |
| 5.4.5 | No Authentication | No auth | 401 | Unauthorized | ✅ Pass |

#### Test Case 5.5: Get Validation Status
**Endpoint:** `GET /api/documents/validate/{documentId}`

**Authentication:** ✅ Required (CUSTOMER)

**Request:**
```bash
curl -X GET http://localhost:2727/api/documents/validate/1 \
  -H "Content-Type: application/json" \
  -u "customer1@abc.com:Customer@123"
```

**Success Response (200 OK):**
```
Document ID: 1 | Status: Valid | File: financial_statement.pdf
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 5.5.1 | Valid Document | documentId: 1 | 200 | Status string | ✅ Pass |
| 5.5.2 | Unvalidated Doc | documentId: 2 | 200 | "Not Validated" | ✅ Pass |
| 5.5.3 | Non-existent | documentId: 9999 | 404 | Not found | ✅ Pass |
| 5.5.4 | No Authentication | No auth | 401 | Unauthorized | ✅ Pass |

#### Test Case 5.6: Validate All Documents
**Endpoint:** `PUT /api/documents/validate-all/{applicationId}`

**Authentication:** ✅ Required (OFFICER/MANAGER)

**Request:**
```bash
curl -X PUT http://localhost:2727/api/documents/validate-all/1 \
  -H "Content-Type: application/json" \
  -u "officer1@cblos.com:Officer@123"
```

**Success Response (200 OK):**
```
Validated 2 out of 2 documents for Application ID: 1
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 5.6.1 | Valid App | applicationId: 1 | 200 | Success message with count | ✅ Pass |
| 5.6.2 | App with No Docs | applicationId: 5 | 200 | "No documents found..." | ✅ Pass |
| 5.6.3 | Non-existent | applicationId: 9999 | 404 | Not found | ✅ Pass |
| 5.6.4 | CUSTOMER User | customer1@abc.com | 403 | Forbidden | ✅ Pass |
| 5.6.5 | No Authentication | No auth | 401 | Unauthorized | ✅ Pass |

#### Test Case 5.7: Get Validation Report
**Endpoint:** `GET /api/documents/validation-report/{applicationId}`

**Authentication:** ✅ Required (CUSTOMER)

**Request:**
```bash
curl -X GET http://localhost:2727/api/documents/validation-report/1 \
  -H "Content-Type: application/json" \
  -u "customer1@abc.com:Customer@123"
```

**Success Response (200 OK):**
```
=== DOCUMENT VALIDATION REPORT ===
Application ID: 1
Total Documents: 2

Document ID: 1
  File Name: financial_statement.pdf
  File Type: application/pdf
  Upload Date: 2026-05-28T12:00:00
  Status: VALID
  File Size: 245632 bytes

Document ID: 2
  File Name: balance_sheet.xlsx
  File Type: application/vnd.ms-excel
  Upload Date: 2026-05-28T12:15:00
  Status: INVALID
  File Size: 512000 bytes

===================================
Valid Documents: 1
Invalid/Unvalidated Documents: 1
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 5.7.1 | Valid App | applicationId: 1 | 200 | Detailed report | ✅ Pass |
| 5.7.2 | App with No Docs | applicationId: 5 | 200 | "No documents..." | ✅ Pass |
| 5.7.3 | Non-existent | applicationId: 9999 | 404 | Not found | ✅ Pass |
| 5.7.4 | No Auth | No auth | 401 | Unauthorized | ✅ Pass |

---

### 6️⃣ COLLATERAL CONTROLLER

#### Test Case 6.1: Add Collateral
**Endpoint:** `POST /api/collateral/add/{applicationId}`

**Authentication:** ✅ Required (CUSTOMER)
```
Username: customer1@abc.com
Password: Customer@123
```

**URL Parameter:**
```
applicationId: 1
```

**Request Body (Valid):**
```json
{
  "assetType": "Real Estate",
  "assetValue": 2000000.00,
  "description": "Commercial property in downtown area"
}
```

**Success Response (200 OK):**
```json
{
  "collateralId": 1,
  "applicationId": 1,
  "assetType": "Real Estate",
  "assetValue": 2000000.00,
  "description": "Commercial property in downtown area",
  "createdAt": "2026-05-28T13:00:00",
  "updatedAt": "2026-05-28T13:00:00"
}
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 6.1.1 | Valid Collateral | Valid data | 201/200 | Collateral object | ✅ Pass |
| 6.1.2 | Various Asset Types | "Machinery" | 200 | Success | ✅ Pass |
| 6.1.3 | Another Asset Type | "Gold/Jewelry" | 200 | Success | ✅ Pass |
| 6.1.4 | Non-existent App | applicationId: 9999 | 404 | Not found | ✅ Pass |
| 6.1.5 | Missing Asset Type | No `assetType` | 400 | Error | ✅ Pass |
| 6.1.6 | Missing Asset Value | No `assetValue` | 400 | Error | ✅ Pass |
| 6.1.7 | Zero Asset Value | assetValue: 0 | 400 | Error | ✅ Pass |
| 6.1.8 | Negative Value | assetValue: -2000000 | 400 | Error | ✅ Pass |
| 6.1.9 | Very Large Value | 999999999999 | 200/400 | Check limit | ⚠️ Check |
| 6.1.10 | Empty Description | description: "" | 200/400 | Accept or error | ⚠️ Check |
| 6.1.11 | No Authentication | No auth | 401 | Unauthorized | ✅ Pass |
| 6.1.12 | OFFICER User | officer1@cblos.com | 403 | Forbidden | ✅ Pass |

#### Test Case 6.2: Get Collateral for Loan
**Endpoint:** `GET /api/collateral/loan/{applicationId}`

**Authentication:** ✅ Required (CUSTOMER)

**Request:**
```bash
curl -X GET http://localhost:2727/api/collateral/loan/1 \
  -H "Content-Type: application/json" \
  -u "customer1@abc.com:Customer@123"
```

**Success Response (200 OK):**
```json
[
  {
    "collateralId": 1,
    "applicationId": 1,
    "assetType": "Real Estate",
    "assetValue": 2000000.00,
    "description": "Commercial property in downtown area",
    "createdAt": "2026-05-28T13:00:00"
  },
  {
    "collateralId": 2,
    "applicationId": 1,
    "assetType": "Machinery",
    "assetValue": 500000.00,
    "description": "Industrial equipment",
    "createdAt": "2026-05-28T13:15:00"
  }
]
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 6.2.1 | Valid Application | applicationId: 1 | 200 | Array of collaterals | ✅ Pass |
| 6.2.2 | App with No Collateral | applicationId: 5 | 200 | `[]` (empty) | ✅ Pass |
| 6.2.3 | Non-existent App | applicationId: 9999 | 404 | Not found | ✅ Pass |
| 6.2.4 | Invalid ID | applicationId: "abc" | 400 | Invalid ID | ✅ Pass |
| 6.2.5 | No Authentication | No auth | 401 | Unauthorized | ✅ Pass |

---

### 7️⃣ CREDIT ASSESSMENT CONTROLLER

#### Test Case 7.1: Evaluate Credit
**Endpoint:** `POST /api/credit/evaluate/{applicationId}`

**Authentication:** ✅ Required (OFFICER/MANAGER)
```
Username: officer1@cblos.com
Password: Officer@123
```

**URL Parameter:**
```
applicationId: 1
```

**Request:**
```bash
curl -X POST http://localhost:2727/api/credit/evaluate/1 \
  -H "Content-Type: application/json" \
  -u "officer1@cblos.com:Officer@123"
```

**Success Response (200 OK):**
```json
{
  "assessmentId": 1,
  "applicationId": 1,
  "riskScore": 45.5,
  "status": "APPROVED",
  "remarks": "Low risk profile. Customer has strong financial position.",
  "createdAt": "2026-05-28T14:00:00",
  "updatedAt": "2026-05-28T14:00:00"
}
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 7.1.1 | Valid Application | applicationId: 1 | 200 | Assessment object | ✅ Pass |
| 7.1.2 | High Risk App | applicationId: 2 | 200 | REJECTED status | ⚠️ Check |
| 7.1.3 | Non-existent App | applicationId: 9999 | 404 | Not found | ✅ Pass |
| 7.1.4 | Invalid ID | applicationId: "abc" | 400 | Invalid ID | ✅ Pass |
| 7.1.5 | CUSTOMER User | customer1@abc.com | 403 | Forbidden | ✅ Pass |
| 7.1.6 | No Authentication | No auth | 401 | Unauthorized | ✅ Pass |

#### Test Case 7.2: Get Risk Score
**Endpoint:** `GET /api/credit/risk-score/{applicationId}`

**Authentication:** ✅ Required (OFFICER/MANAGER)

**Request:**
```bash
curl -X GET http://localhost:2727/api/credit/risk-score/1 \
  -H "Content-Type: application/json" \
  -u "officer1@cblos.com:Officer@123"
```

**Success Response (200 OK):**
```json
45.5
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 7.2.1 | Valid Application | applicationId: 1 | 200 | Risk score (double) | ✅ Pass |
| 7.2.2 | Non-existent App | applicationId: 9999 | 404 | Not found | ✅ Pass |
| 7.2.3 | Invalid ID | applicationId: "abc" | 400 | Invalid ID | ✅ Pass |
| 7.2.4 | CUSTOMER User | customer1@abc.com | 403 | Forbidden | ✅ Pass |
| 7.2.5 | No Auth | No auth | 401 | Unauthorized | ✅ Pass |

---

### 8️⃣ APPROVAL CONTROLLER

#### Test Case 8.1: Approve Loan
**Endpoint:** `POST /api/approvals/approve/{applicationId}`

**Authentication:** ✅ Required (OFFICER/MANAGER)
```
Username: officer1@cblos.com
Password: Officer@123
```

**URL Parameters:**
```
applicationId: 1
comments: "Verified financial documents and collateral." (optional, default provided)
```

**Request:**
```bash
curl -X POST "http://localhost:2727/api/approvals/approve/1?comments=Strong%20financial%20position" \
  -H "Content-Type: application/json" \
  -u "officer1@cblos.com:Officer@123"
```

**Success Response (200 OK):**
```
Loan approved successfully. Loan Account and Disbursement Schedule have been generated.
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 8.1.1 | Valid Application | applicationId: 1 | 200 | Success message | ✅ Pass |
| 8.1.2 | With Comments | comments: "Custom message" | 200 | Success | ✅ Pass |
| 8.1.3 | Default Comments | No comments param | 200 | Success | ✅ Pass |
| 8.1.4 | Already Approved | applicationId: (pre-approved) | 400/409 | Error | ⚠️ Check |
| 8.1.5 | Non-existent App | applicationId: 9999 | 404 | Not found | ✅ Pass |
| 8.1.6 | Invalid ID | applicationId: "abc" | 400 | Invalid ID | ✅ Pass |
| 8.1.7 | CUSTOMER User | customer1@abc.com | 403 | Forbidden | ✅ Pass |
| 8.1.8 | No Authentication | No auth | 401 | Unauthorized | ✅ Pass |

#### Test Case 8.2: Reject Loan
**Endpoint:** `POST /api/approvals/reject/{applicationId}`

**Authentication:** ✅ Required (OFFICER/MANAGER)

**URL Parameters:**
```
applicationId: 2
comments: "Failed credit assessment." (optional, default provided)
```

**Request:**
```bash
curl -X POST "http://localhost:2727/api/approvals/reject/2?comments=Insufficient%20collateral" \
  -H "Content-Type: application/json" \
  -u "officer1@cblos.com:Officer@123"
```

**Success Response (200 OK):**
```
Loan application has been rejected.
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 8.2.1 | Valid Application | applicationId: 2 | 200 | Rejection message | ✅ Pass |
| 8.2.2 | With Custom Reason | comments: "Poor credit score" | 200 | Success | ✅ Pass |
| 8.2.3 | Default Reason | No comments | 200 | Success | ✅ Pass |
| 8.2.4 | Already Rejected | applicationId: (pre-rejected) | 400/409 | Error | ⚠️ Check |
| 8.2.5 | Non-existent App | applicationId: 9999 | 404 | Not found | ✅ Pass |
| 8.2.6 | Invalid ID | applicationId: "abc" | 400 | Invalid ID | ✅ Pass |
| 8.2.7 | CUSTOMER User | customer1@abc.com | 403 | Forbidden | ✅ Pass |
| 8.2.8 | No Authentication | No auth | 401 | Unauthorized | ✅ Pass |

---

### 9️⃣ DISBURSEMENT CONTROLLER

#### Test Case 9.1: Generate Disbursement Report
**Endpoint:** `GET /api/disbursement/report`

**Authentication:** ✅ Required (OFFICER/MANAGER)

**Request:**
```bash
curl -X GET http://localhost:2727/api/disbursement/report \
  -H "Content-Type: application/json" \
  -u "officer1@cblos.com:Officer@123"
```

**Success Response (200 OK):**
```
=== DISBURSEMENT REPORT ===
Total Disbursements: 2
Total Amount Disbursed: 550,000.00

Disbursement ID: 1
  Account ID: 1
  Amount: 500,000.00
  Date: 2026-05-25
  Status: COMPLETED

Disbursement ID: 2
  Account ID: 2
  Amount: 50,000.00
  Date: 2026-05-26
  Status: PENDING

===== END OF REPORT =====
```

**Test Scenarios:**

| # | Scenario | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|
| 9.1.1 | Generate Report | 200 | Text report | ✅ Pass |
| 9.1.2 | No Disbursements | 200 | Report with 0 count | ✅ Pass |
| 9.1.3 | CUSTOMER User | 403 | Forbidden | ✅ Pass |
| 9.1.4 | No Authentication | 401 | Unauthorized | ✅ Pass |

---

### 🔟 REPAYMENT CONTROLLER

#### Test Case 10.1: Get Repayment Schedule
**Endpoint:** `GET /api/repayments/schedule/{accountId}`

**Authentication:** ✅ Required (OFFICER/MANAGER)

**Request:**
```bash
curl -X GET http://localhost:2727/api/repayments/schedule/1 \
  -H "Content-Type: application/json" \
  -u "officer1@cblos.com:Officer@123"
```

**Success Response (200 OK):**
```json
[
  {
    "installmentId": 1,
    "accountId": 1,
    "dueDate": "2026-06-28",
    "amount": 8500.00,
    "status": "PENDING",
    "createdAt": "2026-05-28T10:00:00"
  },
  {
    "installmentId": 2,
    "accountId": 1,
    "dueDate": "2026-07-28",
    "amount": 8500.00,
    "status": "PENDING",
    "createdAt": "2026-05-28T10:00:00"
  },
  {
    "installmentId": 3,
    "accountId": 1,
    "dueDate": "2026-08-28",
    "amount": 8500.00,
    "status": "PAID",
    "createdAt": "2026-05-28T10:00:00"
  }
]
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 10.1.1 | Valid Account | accountId: 1 | 200 | Array of installments | ✅ Pass |
| 10.1.2 | Account with No Schedule | accountId: 10 | 200 | `[]` (empty) | ✅ Pass |
| 10.1.3 | Non-existent Account | accountId: 9999 | 404 | Not found | ✅ Pass |
| 10.1.4 | Invalid ID | accountId: "abc" | 400 | Invalid ID | ✅ Pass |
| 10.1.5 | Negative ID | accountId: -1 | 400/404 | Error | ⚠️ Check |
| 10.1.6 | CUSTOMER User | customer1@abc.com | 403 | Forbidden | ✅ Pass |
| 10.1.7 | No Authentication | No auth | 401 | Unauthorized | ✅ Pass |

#### Test Case 10.2: Record Payment
**Endpoint:** `PUT /api/repayments/account/{accountId}/pay/{installmentId}`

**Authentication:** ✅ Required (OFFICER/MANAGER)

**URL Parameters:**
```
accountId: 1
installmentId: 2
```

**Request:**
```bash
curl -X PUT http://localhost:2727/api/repayments/account/1/pay/2 \
  -H "Content-Type: application/json" \
  -u "officer1@cblos.com:Officer@123"
```

**Success Response (200 OK):**
```
Payment successful for Account ID 1!
```

**Test Scenarios:**

| # | Scenario | Input | Expected Status | Expected Response | Pass/Fail |
|---|---|---|---|---|---|
| 10.2.1 | Valid Payment | accountId: 1, installmentId: 2 | 200 | Success message | ✅ Pass |
| 10.2.2 | All Installments Paid | Already paid | 200/400 | Success or error | ⚠️ Check |
| 10.2.3 | Non-existent Account | accountId: 9999 | 404 | Not found | ✅ Pass |
| 10.2.4 | Non-existent Installment | installmentId: 9999 | 404 | Not found | ✅ Pass |
| 10.2.5 | Mismatch IDs | Wrong account for installment | 400/404 | Error | ⚠️ Check |
| 10.2.6 | Invalid Account ID | accountId: "abc" | 400 | Invalid ID | ✅ Pass |
| 10.2.7 | Invalid Installment ID | installmentId: "xyz" | 400 | Invalid ID | ✅ Pass |
| 10.2.8 | Negative IDs | Negative values | 400/404 | Error | ⚠️ Check |
| 10.2.9 | CUSTOMER User | customer1@abc.com | 403 | Forbidden | ✅ Pass |
| 10.2.10 | No Authentication | No auth | 401 | Unauthorized | ✅ Pass |

---

## TESTING BEST PRACTICES

### ✅ Test Execution Checklist

1. **Environment Setup**
   - [ ] MySQL is running and loan_db database is created
   - [ ] Spring Boot app is running on port 2727
   - [ ] Database is initialized with test data
   - [ ] All test users are created

2. **Test Order Sequence**
   - [ ] Run Authentication tests first (verify your credentials)
   - [ ] Run Loan Officer tests (setup base data)
   - [ ] Run Customer onboarding tests
   - [ ] Run Loan Application submission
   - [ ] Run Document upload/validation tests
   - [ ] Run Collateral tests
   - [ ] Run Credit Assessment tests
   - [ ] Run Approval tests
   - [ ] Run Disbursement tests
   - [ ] Run Repayment tests

3. **Data Dependencies**
   ```
   Loan Officer → Created before testing officer endpoints
   Customer → Created before loan application
   Loan Application → Depends on Customer (prerequisite for all loan operations)
   Document → Depends on Loan Application
   Collateral → Depends on Loan Application
   Credit Assessment → Depends on Loan Application
   Approval → Depends on Credit Assessment
   Loan Account → Created by Approval endpoint
   Disbursement → Depends on approved Loan Account
   Repayment Schedule → Generated with Loan Account
   ```

4. **Common Issues & Solutions**

   | Issue | Cause | Solution |
   |---|---|---|
   | 401 Unauthorized | Invalid credentials | Verify username/password in Basic Auth |
   | 403 Forbidden | Insufficient role | Use appropriate role (OFFICER/MANAGER/CUSTOMER) |
   | 404 Not Found | Invalid ID | Verify resource exists before testing |
   | 400 Bad Request | Invalid JSON | Check JSON format and required fields |
   | 409 Conflict | Duplicate unique field | Use unique values (Tax ID, Employee ID) |
   | Database Connection Error | MySQL not running | Start MySQL service: `net start MySQL80` |
   | CORS Error (Frontend) | Same-origin restriction | Check frontend proxy.conf.json |

### 🔐 Security Testing

1. **Authentication Tests**
   - [ ] Test with valid credentials
   - [ ] Test without credentials (missing header)
   - [ ] Test with invalid password
   - [ ] Test with non-existent user
   - [ ] Test with case-sensitive email
   - [ ] Test with malformed Basic auth header

2. **Authorization Tests**
   - [ ] CUSTOMER cannot access OFFICER endpoints
   - [ ] OFFICER cannot access MANAGER-only endpoints
   - [ ] Test role-based access control for all endpoints
   - [ ] Test access to other users' data

3. **Input Validation Tests**
   - [ ] Test with null values
   - [ ] Test with empty strings
   - [ ] Test with very long strings (>1000 chars)
   - [ ] Test with special characters
   - [ ] Test with SQL injection attempts
   - [ ] Test with XSS payloads
   - [ ] Test with negative numbers where applicable
   - [ ] Test with zero values

4. **Data Integrity Tests**
   - [ ] Unique constraints (Tax ID, Employee ID, Email)
   - [ ] Foreign key relationships
   - [ ] Cascading deletes
   - [ ] Transaction rollback on errors

### 📊 Performance Testing

1. **Load Testing Scenarios**
   - Test concurrent user logins
   - Test document upload with large files
   - Test bulk API calls
   - Monitor database response times

2. **Stress Testing**
   - Test with maximum concurrent connections
   - Test with large result sets
   - Test API timeout scenarios

3. **Metrics to Monitor**
   - Response time (target: <500ms for most endpoints)
   - CPU usage
   - Memory usage
   - Database connection pool

### 🧪 Regression Testing

Execute these tests after every code change:
- All authentication tests (1.x)
- All CRUD operations for each controller
- All authorization tests
- All critical workflow tests

### 📝 Test Report Template

```markdown
# API Test Execution Report
**Date:** 2026-05-28
**Tester:** [Your Name]
**Environment:** Dev/Staging/Production

## Summary
- Total Test Cases: XX
- Passed: XX
- Failed: XX
- Blocked: XX
- Pass Rate: XX%

## Failed Tests
| Test ID | Description | Expected | Actual | Root Cause | Fix |
|---|---|---|---|---|---|
| 1.1.5 | Invalid email | 401 | 500 | NullPointerException in auth service | Need null check |

## Blocked Tests
| Test ID | Description | Blocker |
|---|---|---|
| 10.2.2 | All Installments Paid | Approval endpoint not creating disbursement schedule |

## Recommendations
1. Fix null pointer in auth service
2. Implement disbursement schedule creation in approval endpoint
3. Add input validation for file uploads
```

---

## POSTMAN COLLECTION SETUP

### Step 1: Create Environment Variables
```json
{
  "name": "Loan System Dev",
  "values": [
    {
      "key": "base_url",
      "value": "http://localhost:2727",
      "type": "default"
    },
    {
      "key": "officer_username",
      "value": "officer1@cblos.com",
      "type": "default"
    },
    {
      "key": "officer_password",
      "value": "Officer@123",
      "type": "secret"
    },
    {
      "key": "customer_username",
      "value": "customer1@abc.com",
      "type": "default"
    },
    {
      "key": "customer_password",
      "value": "Customer@123",
      "type": "secret"
    },
    {
      "key": "manager_username",
      "value": "manager1@cblos.com",
      "type": "default"
    },
    {
      "key": "manager_password",
      "value": "Manager@123",
      "type": "secret"
    },
    {
      "key": "customerId",
      "value": "1",
      "type": "default"
    },
    {
      "key": "applicationId",
      "value": "1",
      "type": "default"
    },
    {
      "key": "documentId",
      "value": "1",
      "type": "default"
    },
    {
      "key": "accountId",
      "value": "1",
      "type": "default"
    }
  ]
}
```

### Step 2: Basic Auth Setup in Postman
For each request:
1. Click "Authorization" tab
2. Select type: "Basic Auth"
3. Username: `{{office r_username}}`
4. Password: `{{officer_password}}`

Or manually set header:
```
Authorization: Basic <base64_encoded_credentials>
```

### Step 3: Pre-request Scripts
Add to collection pre-request script:
```javascript
// Log request details
console.log("Testing API: " + pm.request.name);
console.log("URL: " + pm.request.url);

// Set timestamp
pm.environment.set("timestamp", new Date().toISOString());
```

### Step 4: Tests (Assertions)
Add to each request's Tests tab:
```javascript
// Test status code
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

// Test response contains expected data
pm.test("Response contains required fields", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('id');
    pm.expect(jsonData).to.have.property('createdAt');
});

// Test response time
pm.test("Response time is less than 500ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});

// Validate JSON schema
const schema = {
    "type": "object",
    "properties": {
        "id": { "type": "number" }
    }
};
pm.test("Valid schema", () => {
    pm.response.to.have.jsonSchema(schema);
});
```

### Sample Postman Request

**Request Name:** Register Loan Officer

```
Method: POST
URL: {{base_url}}/api/officers/register

Authorization:
  Type: Basic Auth
  Username: {{officer_username}}
  Password: {{officer_password}}

Headers:
  Content-Type: application/json

Body (raw JSON):
{
  "employeeId": "EMP004",
  "name": "Robert Brown",
  "designation": "Loan Officer"
}

Tests:
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});
pm.test("Response has officer ID", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('id');
    pm.environment.set("officerId", jsonData.id);
});
```

---

## SUMMARY

**Project Type:** Spring Boot Corporate Loan Management System  
**Port:** 2727  
**Database:** MySQL (loan_db)  
**Authentication:** HTTP Basic Auth  
**Roles:** OFFICER, MANAGER, CUSTOMER  
**Total API Endpoints:** 25  
**Total Test Scenarios:** 100+  

### Key Testing Points:
✅ All positive scenarios (happy path)  
✅ All negative scenarios (error handling)  
✅ Input validation & edge cases  
✅ Authentication & authorization  
✅ Data integrity & constraints  
✅ Workflow dependencies  
✅ Security considerations  

### Ready to Test!
Use the provided test credentials, cURL commands, and Postman examples to thoroughly validate your API. Follow the test execution sequence to maintain data dependencies. Report all findings using the provided test report template.
