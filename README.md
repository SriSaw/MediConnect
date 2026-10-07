# MediConnect - Healthcare Consultation Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-Migrations%20v11-red.svg)](https://flywaydb.org/)
[![React](https://img.shields.io/badge/React-18%20%2B%20TypeScript-61dafb.svg)](https://react.dev/)
[![Tests](https://img.shields.io/badge/Tests-31%20Passed-success.svg)]()

MediConnect is a full-stack, enterprise-grade healthcare consultation web application engineered as a **modular monolith**. It connects patients with verified healthcare specialists, manages recurring availability schedules, guarantees race-condition-free appointment booking under high concurrency, handles clinical consultations and medical records, and provides comprehensive administrative governance and JDBC-powered reporting.

---

## Table of Contents

1. [Problem Statement & Overview](#problem-statement--overview)
2. [Academic Rubric Evidence Mapping](#academic-rubric-evidence-mapping)
3. [Architecture & System Design](#architecture--system-design)
4. [Technology Stack](#technology-stack)
5. [Database & ER Model](#database--er-model)
6. [Multithreading & Concurrency Strategy](#multithreading--concurrency-strategy)
7. [JDBC Reporting & Direct Servlet Integration](#jdbc-reporting--direct-servlet-integration)
8. [Security & Authentication](#security--authentication)
9. [Prerequisites & Environment Configuration](#prerequisites--environment-configuration)
10. [Local Setup & Startup Guide](#local-setup--startup-guide)
11. [Docker Compose Deployment](#docker-compose-deployment)
12. [API Documentation & Diagnostics](#api-documentation--diagnostics)
13. [Demo Credentials](#demo-credentials)
14. [Testing & Verification](#testing--verification)
15. [Project Structure](#project-structure)

---

## Problem Statement & Overview

Modern healthcare delivery faces severe challenges: double-booked clinical consultations, disconnected medical history records, unverified practitioner profiles, and opaque administrative audit logs.

**MediConnect** solves these issues through a centralized, high-reliability platform that delivers:
- **Verified Practitioner Network**: Multi-specialty directory with licensing verification by system administrators.
- **Conflict-Free Appointment Scheduling**: Deterministic appointment booking with pessimistic row-level locking preventing duplicate bookings under concurrent load.
- **Structured Consultations & Records**: Secure clinical records, follow-up scheduling, and consultation documentation accessible to authorized patients and treating physicians.
- **Administrative Governance & Auditing**: Platform analytics, system settings configuration, and parameterized JDBC reporting over PostgreSQL.

---

## Academic Rubric Evidence Mapping

| Rubric Area | Target | Key Evidence in MediConnect |
| :--- | :--- | :--- |
| **1. Problem Understanding & Solution Design** | 8 Marks | Modular monolith architecture, 3 distinct RBAC personas (Patient, Professional, Admin), complete end-to-end clinical workflow, relational third normal form schema (11 Flyway migrations). |
| **2. Core Java Concepts** | 10 Marks | Interfaces & Abstractions (`AppointmentReportRepository`, `AuditLogQueryRepository`), OOP Encapsulation, Polymorphism via custom exception hierarchy (`MediConnectException` base class), Generics (`PageResponse<T>`), Collections Framework. |
| **3. Database Integration (JDBC)** | 8 Marks | `JdbcTemplate`, low-level manual transaction boundaries (`setAutoCommit(false)`, `commit()`, `rollback()`), parameterized SQL queries, custom `RowMapper` implementations, PostgreSQL execution. |
| **4. Servlets & Web Integration** | 7 Marks | Spring MVC on embedded Apache Tomcat Servlet container, dedicated `SystemDiagnosticsServlet` extending `HttpServlet`, lifecycle hooks (`init`, `doGet`, `destroy`), `ServletRegistrationBean`, RESTful API design. |

### Detailed Rubric Evidence Locator

#### 1. Problem Understanding & Solution Design
- **Architecture**: Clean modular monolith located under `backend/src/main/java/com/mediconnect/` separated by functional domains (`appointment`, `audit`, `auth`, `consultation`, `medicalrecord`, `notification`, `patient`, `professional`, `user`, `settings`).
- **Domain Lifecycles**:
  - Patient registers $\rightarrow$ searches specialists $\rightarrow$ clicks availability window $\rightarrow$ reserves non-conflicting slot.
  - Doctor logs in $\rightarrow$ sets weekly availability windows $\rightarrow$ manages appointments $\rightarrow$ issues clinical consultations.
  - Admin verifies doctors $\rightarrow$ manages user statuses $\rightarrow$ views JDBC reporting & audit logs.
- **Database Schema**: 11 idempotent Flyway migrations located in `backend/src/main/resources/db/migration/`.

#### 2. Core Java Concepts
- **Abstraction & Interfaces**:
  - `AppointmentReportRepository.java`: Decouples report retrieval from underlying JDBC persistence.
  - `AuditLogQueryRepository.java`: Database access interface for parameterized audit reporting.
- **Polymorphism & Exception Hierarchy**:
  - `MediConnectException`: Root abstract domain exception inheriting `RuntimeException`.
  - Subclasses: `BadRequestException`, `ConflictException`, `ForbiddenException`, `ResourceNotFoundException`, `UnauthorizedException`, `DatabaseOperationException`.
  - `GlobalExceptionHandler`: Centralized handler using runtime polymorphism to map exceptions to standardized JSON HTTP responses.
- **Encapsulation & OOP**:
  - Immutable Java records for DTOs (`CreateAppointmentRequest`, `AvailabilityRequest`, `AppointmentReportResponse`).
  - Encapsulated JPA entities with lifecycle hooks (`@PrePersist`, `@PreUpdate`).
- **Collections & Generics**:
  - Generic pagination wrapper `PageResponse<T>` handling typed payload lists across domain boundaries.
  - Comprehensive usage of `List<T>`, `Set<T>`, and `Map<K, V>`.

#### 3. Database Integration (JDBC)
- **JdbcAppointmentReportRepository** (`backend/src/main/java/com/mediconnect/appointment/repository/JdbcAppointmentReportRepository.java`):
  - Direct `JdbcTemplate` parameterized dynamic query execution with multi-table SQL `JOIN`s across `appointments`, `patient_profiles`, `professional_profiles`, and `users`.
  - Type-safe `AppointmentReportRowMapper` converting SQL `ResultSet` columns into typed domain DTOs.
  - Explicit manual JDBC transaction management demonstrating `Connection.setAutoCommit(false)`, batch statement execution, atomic audit logging, `conn.commit()`, and `conn.rollback()` on error.
- **JdbcAnalyticsRepository** (`backend/src/main/java/com/mediconnect/admin/repository/JdbcAnalyticsRepository.java`):
  - Raw SQL aggregation queries (`COUNT`, `GROUP BY`) executing directly on the database.
- **JdbcAuditLogRepository** (`backend/src/main/java/com/mediconnect/audit/JdbcAuditLogRepository.java`):
  - Dynamic parameterized filtering with pagination limits and offsets.

#### 4. Servlets & Web Integration
- **Direct Servlet Integration**:
  - `SystemDiagnosticsServlet` (`backend/src/main/java/com/mediconnect/common/SystemDiagnosticsServlet.java`):
    - Subclasses `jakarta.servlet.http.HttpServlet`.
    - Overrides `init(ServletConfig)`, `doGet(HttpServletRequest, HttpServletResponse)`, and `destroy()`.
    - Uses `HttpServletRequest` headers, remote IP, and parameters.
    - Configures `HttpServletResponse` content type, headers, status codes, and outputs streaming JSON via low-level `PrintWriter`.
    - Directly queries database metadata via `DataSource.getConnection()`.
  - `ServletConfig` (`backend/src/main/java/com/mediconnect/config/ServletConfig.java`):
    - Registers the servlet into Spring Boot's embedded Tomcat container via `ServletRegistrationBean` at `/api/system/servlet-diagnostics`.
- **Spring MVC Integration**:
  - Spring Boot's `DispatcherServlet` handles high-level REST routing, validation, and JSON serialization.

---

## Architecture & System Design

```
+---------------------------------------------------------------------------------+
|                                React 18 SPA (Vite + TS)                         |
|  [Patient Views]          [Doctor Availability]         [Admin JDBC Reports]   |
+---------------------------------------------------------------------------------+
                                        | (HTTPS / REST)
+---------------------------------------------------------------------------------+
|                       Apache Tomcat (Embedded Servlet Engine)                   |
|                                                                                 |
|   +----------------------------------+   +-----------------------------------+  |
|   |   Spring DispatcherServlet       |   |     SystemDiagnosticsServlet      |  |
|   |   (Spring MVC REST Controllers)  |   |   (Direct HttpServlet API)        |  |
|   +----------------------------------+   +-----------------------------------+  |
+---------------------------------------------------------------------------------+
                                        |
+---------------------------------------------------------------------------------+
|                              Spring Service Layer                               |
|   AppointmentService | ProfessionalService | AuthService | AdminService         |
+---------------------------------------------------------------------------------+
                         /                               \
+--------------------------------------+   +--------------------------------------+
|        Spring Data JPA Layer         |   |            Raw JDBC Layer            |
|  AppointmentRepository (Pessimistic) |   |    JdbcAppointmentReportRepository   |
|  AvailabilityRepository              |   |    JdbcAnalyticsRepository           |
|  PatientProfileRepository            |   |    JdbcAuditLogRepository            |
+--------------------------------------+   +--------------------------------------+
                         \                               /
+---------------------------------------------------------------------------------+
|                       PostgreSQL 16 Relational Database                         |
|         (11 Flyway Migrations | Pessimistic Row Locking | Foreign Keys)          |
+---------------------------------------------------------------------------------+
```

---

## Technology Stack

- **Backend**:
  - Java 21 LTS
  - Spring Boot 3.3.4 (Spring MVC, Spring Data JPA, Spring Security, Validation)
  - Jakarta Servlet API 6.0 (Apache Tomcat Embedded)
  - Spring JDBC (`JdbcTemplate`, HikariCP)
  - Flyway Database Migration
  - JJWT (JSON Web Token 0.12.6)
  - Jackson JSR310 Datatype
  - SpringDoc OpenAPI / Swagger 3
- **Frontend**:
  - React 18
  - TypeScript
  - Vite 5
  - Tailwind CSS
  - Lucide Icons & Axios
- **Database**: PostgreSQL 16 (H2 in-memory for unit and concurrency integration tests)
- **Containerization**: Docker & Docker Compose

---

## Database & ER Model

MediConnect utilizes 11 Flyway migrations applied sequentially:
1. `V1__create_users.sql`: User credentials, email unique constraint, role (`ADMIN`, `HEALTHCARE_PROFESSIONAL`, `PATIENT`), user status.
2. `V2__create_patient_profiles.sql`: Demographic details, date of birth, blood group, emergency contact.
3. `V3__create_professional_profiles.sql`: Specialization, license number, consultation fee, verification status.
4. `V4__create_availabilities.sql`: Weekly working windows (`day_of_week`, `start_time`, `end_time`, `available`, check constraints).
5. `V5__create_appointments.sql`: Appointment scheduling (`appointment_date`, `start_time`, `end_time`, `status`, `reason`).
6. `V6__create_consultations.sql`: Medical advice, clinical notes, follow-up dates linked to appointments.
7. `V7__create_medical_records.sql`: Patient medical history entries.
8. `V8__create_notifications.sql`: System alerts, appointment status update notices.
9. `V9__create_messages.sql`: Doctor-patient messaging history.
10. `V10__create_system_settings.sql`: Administrative runtime key-value settings.
11. `V11__create_audit_logs.sql`: Immutable security and transactional audit trail.

---

## Multithreading & Concurrency Strategy

Preventing double bookings under concurrent load is a core requirement of MediConnect.

### Locking & Synchronization Design
1. **Pessimistic Locking**: `ProfessionalProfileRepository.findByIdWithLock(Long id)` executes `SELECT ... FOR UPDATE` via `LockModeType.PESSIMISTIC_WRITE`. This serializes all booking attempts targeting the same specialist within an atomic database transaction.
2. **Transaction Boundary**: The `@Transactional` booking method in `AppointmentService`:
   - Acquires the exclusive row lock on the specialist profile.
   - Evaluates whether the requested time window falls within the registered weekly availability schedules.
   - Executes `findConflictingAppointments()` checking for active (`PENDING`, `CONFIRMED`, `COMPLETED`) overlapping appointments.
   - Executes `findPatientConflictingAppointments()` verifying the patient has no overlapping appointments elsewhere.
   - Persists the new appointment record or throws `ConflictException` (409 Conflict).
3. **Automated Concurrency Proof**:
   - `AppointmentConcurrencyTest.java` launches a thread pool of 5 simultaneous patient threads competing for the exact same specialist and time slot using `CountDownLatch`.
   - **Result**: Exactly 1 booking succeeds; the remaining 4 threads fail deterministically with `ConflictException`. Database verifies exactly 1 persisted appointment.

---

## JDBC Reporting & Direct Servlet Integration

### JDBC Appointment Reporting
In addition to standard JPA persistence, MediConnect provides an administrative reporting engine implemented purely with JDBC:
- **Endpoint**: `GET /api/admin/appointments/reports`
- **Controller**: `AppointmentController.getAppointmentReports`
- **Service**: `AppointmentService.searchAppointmentReports`
- **DAO/Repository**: `JdbcAppointmentReportRepository` implementing `AppointmentReportRepository`
- **Technology**: Uses `JdbcTemplate`, dynamic `WHERE` parameter binding, `LIMIT ? OFFSET ?` pagination, and custom `RowMapper`.
- **UI Demonstration**: Accessible in the Admin portal under **Platform Appointments** by switching between "JDBC Multi-Table SQL Report" and "JPA Standard View".

### Direct Servlet Integration
- **Endpoint**: `GET /api/system/servlet-diagnostics`
- **Class**: `SystemDiagnosticsServlet` registered via `ServletConfig`
- Demonstrates raw `HttpServlet` handling: inspecting client IP, query parameters, setting response headers (`X-Servlet-Engine`), inspecting `DataSource` metadata via raw JDBC connection, and writing formatted JSON directly to `PrintWriter`.

---

## Security & Authentication

- **Authentication**: Stateless JWT token authentication (HMAC-SHA256).
- **Password Security**: Passwords hashed using BCrypt.
- **Role-Based Access Control (RBAC)**:
  - `ADMIN`: Full access to users, system settings, audit logs, and JDBC reports.
  - `HEALTHCARE_PROFESSIONAL`: Availability window management, clinical consultations, schedule access.
  - `PATIENT`: Directory browsing, appointment booking, medical records, personal consultations.
- **Resource-Level Authorization**: Enforces strict user ownership; patients cannot view or cancel other patients' appointments, and doctors cannot alter other doctors' schedules.
- **Security Tests**: Verified by `SecurityIntegrationTest`, `ConsultationSecurityTest`, `MedicalRecordSecurityTest`, and `ProfessionalSecurityTest`.

---

## Prerequisites & Environment Configuration

### Prerequisites
- **Java**: OpenJDK 21 LTS
- **Maven**: Maven 3.9+ (or use the included `./backend/mvnw`)
- **Node.js**: Node 18+ and npm 9+
- **PostgreSQL**: PostgreSQL 16+ (or via Docker)
- **Docker & Docker Compose**: Optional for containerized deployment

### Environment Variables
Copy `.env.example` to `.env` in the project root:

```bash
cp .env.example .env
```

Configure the following variables in `.env`:
```ini
POSTGRES_DB=mediconnect
POSTGRES_USER=mediconnect
POSTGRES_PASSWORD=your_secure_password

DB_HOST=localhost
DB_PORT=5432
DB_NAME=mediconnect
DB_USERNAME=mediconnect
DB_PASSWORD=your_secure_password

JWT_SECRET=your_base64_or_hex_encoded_jwt_secret_key_at_least_256_bits_here
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:80,http://localhost
```

> **Note**: Never commit `.env` or production credentials to source control.

---

## Local Setup & Startup Guide

### 1. Database Setup
Ensure PostgreSQL is running and create the database:
```sql
CREATE DATABASE mediconnect;
CREATE USER mediconnect WITH ENCRYPTED PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE mediconnect TO mediconnect;
```

### 2. Backend Startup
From the `backend` directory:
```bash
cd backend
./mvnw clean spring-boot:run
```
Flyway automatically applies all 11 schema migrations and seeds initial demonstration data. Backend listens on port `8080`.

### 3. Frontend Startup
From the `frontend` directory:
```bash
cd frontend
npm ci
npm run dev
```
Frontend development server starts on `http://localhost:5173`.

---

## Docker Compose Deployment

To build and start the entire stack (PostgreSQL, Spring Boot backend, and Nginx-hosted React frontend):

```bash
docker compose build
docker compose up -d
```

Check running containers:
```bash
docker compose ps
```

- **Frontend Application**: `http://localhost`
- **Backend API**: `http://localhost:8080/api`
- **Swagger Documentation**: `http://localhost:8080/swagger-ui/index.html`
- **Servlet Diagnostics**: `http://localhost:8080/api/system/servlet-diagnostics`

To shut down:
```bash
docker compose down
```

---

## API Documentation & Diagnostics

### Interactive Swagger UI
Explore and execute REST endpoints interactively:
- URL: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI Specification: `http://localhost:8080/v3/api-docs`

### Raw Servlet Diagnostics
Directly verify the low-level Servlet API:
- URL: `http://localhost:8080/api/system/servlet-diagnostics`
- Sample Output:
```json
{
  "servletName": "systemDiagnosticsServlet",
  "timestamp": "2026-10-07T20:15:00.000Z",
  "request": {
    "method": "GET",
    "path": "/api/system/servlet-diagnostics",
    "clientIp": "127.0.0.1",
    "userAgent": "Mozilla/5.0 ...",
    "actionParam": "diagnostics"
  },
  "database": {
    "connected": true,
    "product": "PostgreSQL",
    "version": "16.0"
  },
  "jvm": {
    "javaVersion": "21.0.12.1",
    "freeMemoryBytes": 142606336,
    "totalMemoryBytes": 268435456
  },
  "executionDurationMs": 2
}
```

---

## Demo Credentials

All seed accounts use the default password: **`Password123!`**

| Role | Email | Purpose / Demonstrable Capabilities |
| :--- | :--- | :--- |
| **Admin** | `admin@mediconnect.local` | Platform dashboard, user management, audit logs, and **JDBC SQL Appointment Reports**. |
| **Doctor (Cardiology)** | `doctor1@mediconnect.local` | Manage weekly availability windows, review scheduled appointments, consultations. |
| **Doctor (Dermatology)** | `doctor2@mediconnect.local` | Consultations, schedule availability. |
| **Patient 1** | `patient1@mediconnect.local` | Browse specialists, select available time windows, book consultations, medical history. |
| **Patient 2** | `patient2@mediconnect.local` | Alternative patient for testing booking and concurrency. |

---

## Testing & Verification

MediConnect maintains comprehensive unit and integration test coverage protecting domain invariants, concurrency, security, and JDBC operations.

### Run All Backend Tests
```bash
cd backend
./mvnw test
```
**Results**: `31 tests run, 0 failures, 0 errors, 0 skipped`

Key test classes:
- `AppointmentConcurrencyTest`: Verifies pessimistic locking prevents double booking under 5-thread contention.
- `AppointmentFlowIntegrationTest`: End-to-end MockMvc verification of appointment booking, conflict handling, and availability addition.
- `JdbcRepositoriesIntegrationTest`: Verifies `JdbcAppointmentReportRepository`, `JdbcAnalyticsRepository`, and `JdbcAuditLogRepository`.
- `SystemDiagnosticsServletTest`: Verifies `HttpServlet` lifecycle, headers, and PrintWriter response generation.
- `SecurityIntegrationTest`: Enforces authentication and RBAC boundaries across endpoints.
- `DatabaseMigrationAndSchemaTest`: Verifies all 11 Flyway migrations apply correctly to PostgreSQL/H2.

### Run Frontend Build
```bash
cd frontend
npm run build
```
Ensures complete TypeScript compilation (`tsc`) and Vite bundling with zero errors.

---

## Project Structure

```
MediConnect/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/mediconnect/
│   │   │   │   ├── admin/             # Admin controllers, service, JDBC analytics
│   │   │   │   ├── appointment/       # Appointment booking, concurrency, JDBC report repository
│   │   │   │   ├── audit/             # Audit logging service and JDBC repository
│   │   │   │   ├── auth/              # JWT authentication and user registration
│   │   │   │   ├── common/            # SystemDiagnosticsServlet, PageResponse
│   │   │   │   ├── config/            # JacksonConfig, ServletConfig, DataInitializer
│   │   │   │   ├── consultation/      # Post-appointment clinical consultation notes
│   │   │   │   ├── exception/         # Custom exception hierarchy, GlobalExceptionHandler
│   │   │   │   ├── medicalrecord/     # Clinical records and diagnoses
│   │   │   │   ├── notification/      # System notifications
│   │   │   │   ├── patient/           # Patient profiles and medical history
│   │   │   │   ├── professional/      # Specialist profiles and availability schedules
│   │   │   │   ├── security/          # JWT filters, UserPrincipal, SecurityConfig
│   │   │   │   └── settings/          # Platform runtime system settings
│   │   │   └── resources/
│   │   │       ├── db/migration/      # 11 Flyway SQL schema migrations
│   │   │       └── application.yml    # Application configuration
│   │   └── test/                      # 31 unit, concurrency, security, and JDBC tests
│   ├── Dockerfile
│   ├── mvnw
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── api/                       # Axios client with JWT refresh interceptors
│   │   ├── components/                # Reusable navigation and UI components
│   │   ├── pages/                     # BookAppointment, DoctorAvailability, AdminAppointments (JDBC)
│   │   ├── services/                  # Typed API services
│   │   └── types/                     # TypeScript domain models
│   ├── Dockerfile
│   ├── package.json
│   └── vite.config.ts
├── docker-compose.yml
├── .env.example
└── README.md
```
