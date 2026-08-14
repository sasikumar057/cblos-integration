# CBLOS - Corporate Loan Origination System

CBLOS is a corporate lending workflow application. Companies register, admins verify them, customers apply for loans, officers review credit risk and documents, managers approve or reject, and approved loans move into repayment.

## Tech Stack

- Backend: Spring Boot, Java 21, Spring Security, Spring Data JPA
- Frontend: Angular
- Database: MySQL
- Build tools: Maven, npm

## Prerequisites

- Java JDK 21
- Maven
- Node.js and npm
- MySQL Server

## Database Setup

Create the database:

```sql
CREATE DATABASE loan_db8;
```

Update database credentials if needed:

```text
src/main/resources/application.properties
```

Current default:

```properties
server.port=2727
spring.datasource.url=jdbc:mysql://localhost:3306/loan_db8
spring.datasource.username=root
spring.datasource.password=MySQL@2025!Secure#Db$X9zL
spring.jpa.hibernate.ddl-auto=update
```

## Run Backend

From the project root:

```bash
mvn spring-boot:run
```

Backend URL:

```text
http://localhost:2727/
```

## Run Frontend

From the frontend folder:

```bash
cd frontend
npm install
npm start
```

Frontend URL:

```text
http://localhost:4200/
```

## Default Admin Login

```text
Email: admin@bank.com
Password: password
```

The backend seeds the admin user and loan products during startup.

## Main Roles

- Admin: approves customer registration and onboards bank staff.
- Customer: registers, applies for loans, uploads documents, tracks status, and repays loans.
- Officer: reviews customer applications, documents, collateral, and credit score.
- Manager: gives final approval or rejection and triggers disbursement.

## Demo Flow

1. Open home page: `http://localhost:4200/`
2. Register a customer from the Register page.
3. Login as admin.
4. Approve the pending customer.
5. Onboard an officer and manager from admin dashboard.
6. Login as customer.
7. Select a loan product and start application.
8. Add collateral and collateral proof document.
9. Upload tax return and business license.
10. Application moves to officer shared queue.
11. Login as officer.
12. Review application and add credit score.
13. If score is below 600, application is rejected.
14. Otherwise escalate to manager.
15. Login as manager and approve/reject.
16. If approved, customer can view active loan and repay installments.

## Important Statuses

- `PENDING_VERIFICATION`: customer registration waiting for admin.
- `ACTIVE`: customer approved and can login.
- `DOCUMENT_PENDING`: application started but documents are incomplete.
- `UNDER_REVIEW`: documents complete and visible to officers.
- `PENDING_MANAGER_APPROVAL`: officer escalated to manager.
- `APPROVED`: manager approved and loan account is created.
- `REJECTED`: application rejected by officer or manager.
- `WITHDRAWN`: customer removed application from active pipeline.

## Key Backend Files

- `src/main/java/com/cblos/controller` - REST API controllers
- `src/main/java/com/cblos/service` - business logic
- `src/main/java/com/cblos/repository` - database access
- `src/main/java/com/cblos/model` - JPA entities
- `src/main/java/com/cblos/security` - authentication and role access

## Key Frontend Files

- `frontend/src/app/app.routes.ts` - Angular routes and guards
- `frontend/src/app/core/services` - API services
- `frontend/src/app/pages` - application pages and dashboards
- `frontend/src/app/core/guards` - auth and role guards
- `frontend/src/app/core/utils` - timeline, status, and credit intelligence helpers

## How REST Flow Works

Angular component calls an Angular service. The service uses `HttpClient` to call Spring Boot REST API. The Spring controller receives the request, the service applies business logic, the repository saves or reads data from MySQL, and JSON is returned to Angular.

Example:

```text
LoanApplyComponent
-> LoanService
-> LoanApplicationController
-> LoanApplicationService
-> LoanApplicationRepository
-> MySQL
```

## Required Loan Documents

Each loan application needs:

- Collateral Proof
- Tax Return
- Business License

Allowed uploads:

- PDF
- JPG
- PNG
- Maximum size: 5MB

## Troubleshooting

- If backend does not start, check whether port `2727` is already in use.
- If frontend cannot load data, confirm backend is running on `2727`.
- If login fails, confirm MySQL is running and the backend started successfully.
- If products are missing, restart backend so product seeding runs.
- If uploads fail, check file type and size.

## Short Explanation For Evaluation

CBLOS uses Angular for the frontend and Spring Boot REST APIs for the backend. Angular pages call services, services call backend endpoints, controllers receive requests, service classes apply business rules, repositories interact with MySQL, and role-based security protects admin, customer, officer, and manager workflows.
