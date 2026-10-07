CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patient_profiles(id) ON DELETE CASCADE,
    professional_id BIGINT NOT NULL REFERENCES professional_profiles(id) ON DELETE CASCADE,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    reason TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_appt_time_order CHECK (start_time < end_time)
);

CREATE INDEX idx_appt_patient ON appointments(patient_id);
CREATE INDEX idx_appt_prof ON appointments(professional_id);
CREATE INDEX idx_appt_date ON appointments(appointment_date);
CREATE INDEX idx_appt_prof_date_time ON appointments(professional_id, appointment_date, start_time, end_time);
CREATE INDEX idx_appt_status ON appointments(status);
