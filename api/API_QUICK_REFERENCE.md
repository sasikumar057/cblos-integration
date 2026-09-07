# 🚀 LOAN SYSTEM API - QUICK REFERENCE GUIDE

## Project Information
- **Project Name:** Corporate Loan Management System (CBLOS)
- **Base URL:** `http://localhost:2727`
- **Port:** 2727
- **Database:** MySQL (loan_db)
- **Java Version:** Java 21
- **Framework:** Spring Boot 4.0.6
- **Authentication:** HTTP Basic Auth
- **Status:** Active & Running ✅

---

## ⚡ Quick Start

### 1. Ensure Services are Running
```bash
# MySQL
net start MySQL80              # Windows
# or stop/start via Services GUI

# Spring Boot Application
cd c:\Users\2483979\OneDrive - Cognizant\Desktop\project\V3-fullworking\demo-
mvn spring-boot:run
# App will be available at: http://localhost:2727
```

### 2. Check System Status
```bash
# Verify Backend is Running
curl -X GET http://localhost:2727/api/auth/me \
  -u "officer1@cblos.com:Officer@123"

# Expected Response: User details with role
```

### 3. Run Test Script
```bash
# PowerShell (Windows)
.\test_api.ps1                    # Interactive mode
.\test_api.ps1 workflow           # Quick workflow test

# Bash (Linux/Mac)
bash test_api.sh
bash test_api.sh workflow
```

---

## 🔐 Test Users (Pre-configured)

### Loan Officers
```
Email: officer1@cblos.com
Password: Officer@123
Role: OFFICER
Employee ID: EMP001

Email: officer2@cblos.com
Password: Officer@123
Role: OFFICER
Employee ID: EMP002
```

### Managers
```
Email: manager1@cblos.com
Password: Manager@123
Role: MANAGER
Employee ID: MGR001
```

### Customers
```
Email: customer1@abc.com
Password: Customer@123
Role: CUSTOMER
Company: ABC Corporation
Tax ID: TAX001

Email: customer2@xyz.com
Password: Customer@123
Role: CUSTOMER
Company: XYZ Industries
Tax ID: TAX002
```

---

## 🔗 Core API Endpoints

### Authentication
```
GET  /api/auth/me
     → Get current user info
     → Role: Any Authenticated
```

### Loan Officers (OFFICER/MANAGER only)
```
POST /api/officers/register
     → Register new loan officer
     → Role: OFFICER, MANAGER

GET  /api/officers/{id}
     → Get officer by ID

GET  /api/officers/all
     → Get all loan officers
```

### Customers
```
POST /api/customers/onboard
     → Onboard new customer
     → Role: OFFICER, MANAGER

GET  /api/customers/{id}
     → Get customer by ID
     → Role: Any Authenticated

GET  /api/customers/all
     → Get all customers
     → Role: OFFICER, MANAGER
```

### Loan Applications
```
POST /api/loans/submit/{customerId}
     → Submit loan application
     → Role: CUSTOMER

GET  /api/loans/status/{id}
     → Get application status
     → Role: CUSTOMER

GET  /api/loans/{id}
     → Get full application details
     → Role: CUSTOMER

GET  /api/loans/all
     → Get all applications
     → Role: Any Authenticated
```

### Documents
```
POST /api/documents/upload/{applicationId}
     → Upload document
     → Role: CUSTOMER

GET  /api/documents/download/{documentId}
     → Download document

GET  /api/documents/application/{applicationId}
     → List documents in application

PUT  /api/documents/validate/{documentId}
     → Validate single document
     → Role: OFFICER, MANAGER

GET  /api/documents/validate/{documentId}
     → Get validation status

PUT  /api/documents/validate-all/{applicationId}
     → Validate all documents
     → Role: OFFICER, MANAGER

GET  /api/documents/validation-report/{applicationId}
     → Get validation report
```

### Collateral
```
POST /api/collateral/add/{applicationId}
     → Add collateral to application
     → Role: CUSTOMER

GET  /api/collateral/loan/{applicationId}
     → Get collateral for application
     → Role: CUSTOMER
```

### Credit Assessment
```
POST /api/credit/evaluate/{applicationId}
     → Evaluate credit risk
     → Role: OFFICER, MANAGER

GET  /api/credit/risk-score/{applicationId}
     → Get risk score
     → Role: OFFICER, MANAGER
```

### Approvals
```
POST /api/approvals/approve/{applicationId}
     → Approve loan
     → Role: OFFICER, MANAGER
     → Query Param: ?comments=Your%20message

POST /api/approvals/reject/{applicationId}
     → Reject loan
     → Role: OFFICER, MANAGER
     → Query Param: ?comments=Reason%20here
```

### Disbursement
```
GET  /api/disbursement/report
     → Generate disbursement report
     → Role: OFFICER, MANAGER
```

### Repayments
```
GET  /api/repayments/schedule/{accountId}
     → Get repayment schedule
     → Role: OFFICER, MANAGER

PUT  /api/repayments/account/{accountId}/pay/{installmentId}
     → Record payment
     → Role: OFFICER, MANAGER
```

---

## 📌 Common Test Cases

### Test Case 1: Register New Officer
```bash
curl -X POST http://localhost:2727/api/officers/register \
  -H "Content-Type: application/json" \
  -u "officer1@cblos.com:Officer@123" \
  -d '{
    "employeeId": "EMP-NEW-001",
    "name": "John Doe",
    "designation": "Loan Officer"
  }'
```

**Expected Response (200):**
```json
{
  "id": 3,
  "employeeId": "EMP-NEW-001",
  "name": "John Doe",
  "designation": "Loan Officer"
}
```

---

### Test Case 2: Onboard Customer
```bash
curl -X POST http://localhost:2727/api/customers/onboard \
  -H "Content-Type: application/json" \
  -u "officer1@cblos.com:Officer@123" \
  -d '{
    "taxId": "TAX-NEW-2026",
    "companyName": "Tech Corp",
    "industryType": "Technology"
  }'
```

**Expected Response (200):**
```json
{
  "id": 4,
  "taxId": "TAX-NEW-2026",
  "companyName": "Tech Corp",
  "industryType": "Technology"
}
```

---

### Test Case 3: Submit Loan Application
```bash
curl -X POST http://localhost:2727/api/loans/submit/1 \
  -H "Content-Type: application/json" \
  -u "customer1@abc.com:Customer@123" \
  -d '{
    "requestedAmount": 500000.00,
    "tenure": 60,
    "purpose": "Working Capital",
    "status": "APPLIED"
  }'
```

**Expected Response (200):**
```json
{
  "applicationId": 1,
  "corporateCustomerId": 1,
  "requestedAmount": 500000.00,
  "tenure": 60,
  "purpose": "Working Capital",
  "status": "APPLIED"
}
```

---

### Test Case 4: Add Collateral
```bash
curl -X POST http://localhost:2727/api/collateral/add/1 \
  -H "Content-Type: application/json" \
  -u "customer1@abc.com:Customer@123" \
  -d '{
    "assetType": "Real Estate",
    "assetValue": 2000000.00,
    "description": "Commercial building"
  }'
```

**Expected Response (200):**
```json
{
  "collateralId": 1,
  "applicationId": 1,
  "assetType": "Real Estate",
  "assetValue": 2000000.00,
  "description": "Commercial building"
}
```

---

### Test Case 5: Evaluate Credit
```bash
curl -X POST http://localhost:2727/api/credit/evaluate/1 \
  -H "Content-Type: application/json" \
  -u "officer1@cblos.com:Officer@123"
```

**Expected Response (200):**
```json
{
  "assessmentId": 1,
  "applicationId": 1,
  "riskScore": 45.5,
  "status": "APPROVED",
  "remarks": "Low risk profile"
}
```

---

### Test Case 6: Approve Loan
```bash
curl -X POST "http://localhost:2727/api/approvals/approve/1?comments=Verified%20documents" \
  -H "Content-Type: application/json" \
  -u "officer1@cblos.com:Officer@123"
```

**Expected Response (200):**
```
Loan approved successfully. Loan Account and Disbursement Schedule have been generated.
```

---

### Test Case 7: Get Repayment Schedule
```bash
curl -X GET http://localhost:2727/api/repayments/schedule/1 \
  -H "Content-Type: application/json" \
  -u "officer1@cblos.com:Officer@123"
```

**Expected Response (200):**
```json
[
  {
    "installmentId": 1,
    "accountId": 1,
    "dueDate": "2026-06-28",
    "amount": 8500.00,
    "status": "PENDING"
  },
  {
    "installmentId": 2,
    "accountId": 1,
    "dueDate": "2026-07-28",
    "amount": 8500.00,
    "status": "PENDING"
  }
]
```

---

## ❌ Common Error Responses

### 401 Unauthorized (Bad Credentials)
```json
{
  "error": "Unauthorized",
  "message": "Invalid username or password"
}
```
**Cause:** Wrong credentials or missing Authorization header

---

### 403 Forbidden (Insufficient Permissions)
```json
{
  "error": "Forbidden",
  "message": "You do not have permission to access this resource"
}
```
**Cause:** User role doesn't have permission (e.g., CUSTOMER trying to approve loan)

---

### 404 Not Found
```json
{
  "error": "Not Found",
  "message": "Resource with ID 9999 does not exist"
}
```
**Cause:** Invalid ID, resource doesn't exist

---

### 400 Bad Request (Validation Error)
```json
{
  "error": "Bad Request",
  "message": "Requested amount must be greater than 0"
}
```
**Cause:** Invalid input, missing required field, or constraint violation

---

### 409 Conflict (Duplicate)
```json
{
  "error": "Conflict",
  "message": "Tax ID already exists"
}
```
**Cause:** Duplicate value in unique field (Tax ID, Employee ID, Email, etc.)

---

## 🔄 Complete Workflow Sequence

### Happy Path Flow:
1. **Officer Login** → `/api/auth/me` (Verify Officer Access)
2. **Onboard Customer** → POST `/api/customers/onboard`
3. **Customer Login** → `/api/auth/me` (Verify Customer Access)
4. **Submit Loan** → POST `/api/loans/submit/{customerId}`
5. **Add Collateral** → POST `/api/collateral/add/{applicationId}`
6. **Upload Documents** → POST `/api/documents/upload/{applicationId}` (with file)
7. **Officer: Validate Docs** → PUT `/api/documents/validate/{documentId}`
8. **Officer: Evaluate Credit** → POST `/api/credit/evaluate/{applicationId}`
9. **Officer: Check Risk Score** → GET `/api/credit/risk-score/{applicationId}`
10. **Officer: Approve Loan** → POST `/api/approvals/approve/{applicationId}`
11. **View Repayment Schedule** → GET `/api/repayments/schedule/{accountId}`
12. **Record Payments** → PUT `/api/repayments/account/{accountId}/pay/{installmentId}`

---

## 📋 Testing Checklist

### Setup
- [ ] MySQL is running and loan_db database exists
- [ ] Spring Boot application is running on port 2727
- [ ] All test users are created with correct passwords
- [ ] Network connectivity to localhost:2727 verified

### Authentication Tests
- [ ] Login with OFFICER credentials ✓
- [ ] Login with MANAGER credentials ✓
- [ ] Login with CUSTOMER credentials ✓
- [ ] Reject invalid credentials ✓
- [ ] Reject missing Authorization header ✓

### CRUD Operations
- [ ] Create (POST) - All entities
- [ ] Read (GET) - All entities
- [ ] Update (PUT) - Applicable endpoints
- [ ] List (GET /all) - Each resource type

### Authorization Tests
- [ ] OFFICER can access /api/officers/** ✓
- [ ] CUSTOMER cannot access /api/officers/** ✓
- [ ] OFFICER can manage approvals ✓
- [ ] CUSTOMER can submit loans ✓
- [ ] OFFICER can evaluate credit ✓

### Workflow Tests
- [ ] Complete loan approval workflow ✓
- [ ] Complete loan rejection workflow ✓
- [ ] Complete payment recording ✓
- [ ] Multi-collateral addition ✓
- [ ] Document upload and validation ✓

### Error Handling
- [ ] 401 for unauthenticated requests ✓
- [ ] 403 for insufficient permissions ✓
- [ ] 404 for non-existent resources ✓
- [ ] 400 for invalid input ✓
- [ ] 409 for duplicate entries ✓

### Performance Tests
- [ ] Response time < 500ms ✓
- [ ] Concurrent requests handled ✓
- [ ] Large file uploads (if applicable) ✓
- [ ] Database constraints enforced ✓

---

## 🛠️ Troubleshooting

| Issue | Cause | Solution |
|---|---|---|
| 401 Unauthorized | Wrong credentials | Verify username and password are correct |
| 403 Forbidden | User role insufficient | Use appropriate role for the endpoint |
| 404 Not Found | Invalid resource ID | Verify resource exists (check GET /all first) |
| Connection Refused | App not running | Start Spring Boot: `mvn spring-boot:run` |
| MySQL Connection Error | Database not running | Start MySQL: `net start MySQL80` |
| 500 Server Error | Backend error | Check application logs for stack trace |
| CORS Error (Frontend) | Same-origin restriction | Check frontend proxy configuration |
| Invalid JSON | Malformed request body | Validate JSON syntax in POST/PUT body |

---

## 📊 Testing in Postman

### Import Collection
1. Download `Postman_Collection.json`
2. Open Postman → File → Import → Select JSON file
3. Collection automatically loads with all endpoints

### Configure Environment
1. Create new environment named "Loan System Dev"
2. Add variables:
   - `base_url` = `http://localhost:2727`
   - `officer_username` = `officer1@cblos.com`
   - `officer_password` = `Officer@123`
   - `customer_username` = `customer1@abc.com`
   - `customer_password` = `Customer@123`

### Run Requests
1. Select environment: "Loan System Dev"
2. Click on request in collection
3. Click "Send"
4. View response in lower panel

### Run Tests in Sequence
1. Open Collection → Run
2. Select all requests
3. Click "Run" button
4. View results in Test Results window

---

## 💾 Database Queries (MySQL)

### View All Users
```sql
SELECT id, email, role, corporate_customer_id, loan_officer_id FROM app_user;
```

### View All Customers
```sql
SELECT id, tax_id, company_name, industry_type FROM corporate_customer;
```

### View All Loan Applications
```sql
SELECT application_id, corporate_customer_id, requested_amount, tenure, status FROM loan_application;
```

### View All Documents
```sql
SELECT document_id, loan_application_id, file_name, file_type, is_valid, upload_date FROM document;
```

### View All Repayment Schedules
```sql
SELECT installment_id, account_id, due_date, amount, status FROM repayment_schedule;
```

### Clear Test Data (Use with Caution!)
```sql
-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Clear tables
DELETE FROM repayment_schedule;
DELETE FROM disbursement;
DELETE FROM loan_account;
DELETE FROM approval;
DELETE FROM credit_assessment;
DELETE FROM document;
DELETE FROM collateral;
DELETE FROM loan_application;
DELETE FROM corporate_customer;
DELETE FROM loan_officer;
DELETE FROM app_user;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;
```

---

## 🎯 Key Testing Principles

1. **Test in Order**: Follow the dependency chain (officers → customers → applications → etc.)
2. **Verify IDs**: Always note created IDs for use in subsequent requests
3. **Use Real Data**: Test with realistic values (amounts, dates, descriptions)
4. **Check Headers**: Ensure "Content-Type: application/json" is set
5. **Validate Responses**: Check response codes and data structure
6. **Test Negatives**: Try invalid inputs, missing fields, wrong roles
7. **Document Results**: Keep track of what works and what doesn't
8. **Repeat Tests**: Run same test multiple times to ensure consistency

---

## 📞 Support Information

- **Framework:** Spring Boot 4.0.6
- **Java Version:** 21
- **Database:** MySQL 8.0+
- **Port:** 2727
- **Architecture:** REST API with Role-Based Access Control
- **Status:** Production Ready

For issues or questions, check the project documentation or review API logs in the Spring Boot console.

---

**Last Updated:** May 28, 2026  
**API Version:** v1.0  
**Status:** ✅ Active
