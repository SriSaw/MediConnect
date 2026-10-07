# MediConnect Database Schema & Migration Guide

## Migration History
All database changes are managed strictly via Flyway under `backend/src/main/resources/db/migration/`:

1. `V1__create_users.sql`: Platform user accounts (`id`, `name`, `email`, `password_hash`, `role`, `status`).
2. `V2__create_patient_profiles.sql`: Demographics, blood group, emergency contact details.
3. `V3__create_professional_profiles.sql`: Credentials, licensing, specialization, fees, verified flag.
4. `V4__create_availabilities.sql`: Weekly provider working hours with time constraint checks.
5. `V5__create_appointments.sql`: Booking records with patient/doctor foreign keys and time ordering.
6. `V6__create_consultations.sql`: Clinical notes, official medical advice, follow-up dates.
7. `V7__create_medical_records.sql`: Patient medical history entries.
8. `V8__create_notifications.sql`: User notification inbox.
9. `V9__create_messages.sql`: Direct messaging thread entries.
10. `V10__create_system_settings.sql`: Global configuration parameters.
11. `V11__create_audit_logs.sql`: Immutable compliance audit trail.

## Concurrency & Integrity
- Foreign keys maintain referential integrity with cascading or nullifying policies.
- Check constraints ensure `start_time < end_time` on availabilities and appointments.
- Unique constraints protect email uniqueness and appointment-to-consultation one-to-one mapping.
