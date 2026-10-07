CREATE TABLE consultations (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL UNIQUE REFERENCES appointments(id) ON DELETE CASCADE,
    patient_id BIGINT NOT NULL REFERENCES patient_profiles(id) ON DELETE CASCADE,
    professional_id BIGINT NOT NULL REFERENCES professional_profiles(id) ON DELETE CASCADE,
    notes TEXT,
    medical_advice TEXT NOT NULL,
    follow_up_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_consult_appointment ON consultations(appointment_id);
CREATE INDEX idx_consult_patient ON consultations(patient_id);
CREATE INDEX idx_consult_prof ON consultations(professional_id);
