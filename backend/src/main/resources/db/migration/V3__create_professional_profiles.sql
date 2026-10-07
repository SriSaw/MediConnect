CREATE TABLE professional_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    specialization VARCHAR(100) NOT NULL,
    license_number VARCHAR(100) NOT NULL UNIQUE,
    experience_years INTEGER NOT NULL DEFAULT 0,
    bio TEXT,
    consultation_fee NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_prof_profiles_user_id ON professional_profiles(user_id);
CREATE INDEX idx_prof_profiles_specialization ON professional_profiles(specialization);
CREATE INDEX idx_prof_profiles_verified ON professional_profiles(verified);
