# MediConnect Architecture & System Design

## Overview
MediConnect is a role-based telehealth and healthcare consultation platform architected as a clean modular monolith adhering to package-by-feature principles.

### Key Pillars
- **Zero Trust Role-Based Access Control**: Granular endpoint authorization (`ADMIN`, `HEALTHCARE_PROFESSIONAL`, `PATIENT`) coupled with row-level ownership assertions.
- **Transactional Double-Booking Prevention**: Pessimistic database row locking (`PESSIMISTIC_WRITE`) alongside availability interval matching.
- **Permanent Audit Trail**: Regulatory logging of all credential issues, clinical record accesses, and appointment state alterations.

## Backend Architecture
- **Framework**: Spring Boot 3.3.4 on Java 21
- **Security**: Stateless Spring Security filter chain with HMAC-SHA256 JWT validation and Bearer tokens.
- **Data Layer**: Spring Data JPA + Hibernate 6.5 with Flyway schema migration.
- **Database**: PostgreSQL 16
- **Documentation**: Springdoc OpenAPI / Swagger UI at `/swagger-ui.html`

### Package-By-Feature Structure
```
backend/src/main/java/com/mediconnect/
├── admin/          # User administration, status updates, analytics overview
├── appointment/    # Concurrency-safe booking, cancellation, scheduling
├── audit/          # Security and access event logging
├── auth/           # Login, registration, JWT refresh, UserPrincipal
├── common/         # Generic pagination wrappers and utilities
├── config/         # OpenAPI configuration, demo data seeding
├── consultation/   # Clinical diagnosis, advice recording, follow-up dates
├── exception/      # Centralized GlobalExceptionHandler
├── medicalrecord/  # Patient clinical history and doctor access controls
├── message/        # Direct REST patient-doctor messaging
├── notification/   # User notification inbox and read-state management
├── patient/        # Patient profiles and demographics
├── professional/   # Healthcare provider profiles and availability windows
├── security/       # JWT filter, EntryPoint, AccessDeniedHandler, SecurityConfig
├── settings/       # Global system configuration key-value store
└── user/           # User entity, roles, status enum
```
