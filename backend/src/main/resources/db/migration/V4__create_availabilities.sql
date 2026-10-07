CREATE TABLE availabilities (
    id BIGSERIAL PRIMARY KEY,
    professional_id BIGINT NOT NULL REFERENCES professional_profiles(id) ON DELETE CASCADE,
    day_of_week VARCHAR(15) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_avail_time_order CHECK (start_time < end_time)
);

CREATE INDEX idx_avail_prof_day ON availabilities(professional_id, day_of_week);
