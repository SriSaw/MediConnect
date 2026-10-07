# MediConnect REST API Reference

All protected endpoints require an `Authorization: Bearer <JWT>` header.

## Authentication (`/api/auth`)
- `POST /api/auth/register`: Register new user (`PATIENT` or `HEALTHCARE_PROFESSIONAL`)
- `POST /api/auth/login`: Authenticate and obtain JWT access & refresh tokens
- `POST /api/auth/refresh`: Issue fresh access token using valid refresh token
- `GET /api/auth/me`: Fetch profile of authenticated principal

## Appointments (`/api/appointments`)
- `POST /api/appointments`: Book consultation (Patient only, double-booking prevention)
- `GET /api/appointments/me`: Patient appointments list (Paginated)
- `GET /api/professionals/me/appointments`: Doctor appointments list (Paginated)
- `GET /api/appointments/{id}`: Single appointment details (Participant or Admin)
- `PATCH /api/appointments/{id}/cancel`: Cancel scheduled appointment

## Consultations (`/api/consultations`)
- `POST /api/consultations`: Record diagnosis and medical advice (Doctor only)
- `GET /api/consultations/me`: User consultations list (Paginated)
- `GET /api/consultations/{id}`: Consultation details by ID
- `PUT /api/consultations/{id}`: Amend medical advice (Assigned doctor only)

## Medical Records (`/api/medical-records`)
- `GET /api/medical-records/me`: Patient view of own records
- `POST /api/medical-records/me`: Create patient record
- `PUT /api/medical-records/me/{id}`: Update patient record
- `DELETE /api/medical-records/me/{id}`: Remove patient record
- `GET /api/medical-records/patient/{patientId}`: Doctor view of patient records (requires consultation relationship)

## Administration (`/api/admin`)
- `GET /api/admin/users`: Search and filter users
- `PATCH /api/admin/users/{id}/status`: Set `ACTIVE`, `INACTIVE`, `SUSPENDED`
- `PATCH /api/admin/users/{id}/role`: Elevate or update user role
- `PATCH /api/admin/professionals/{id}/verify`: Verify provider license
- `GET /api/admin/analytics/overview`: Platform operational statistics
- `GET /api/admin/audit-logs`: Security audit trail
- `GET /api/admin/settings`: System settings configuration
