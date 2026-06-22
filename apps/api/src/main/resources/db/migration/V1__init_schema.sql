-- ============================================================
-- EXTENSIONS
-- ============================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "vector";

-- ============================================================
-- ENUM TYPES
-- ============================================================
CREATE TYPE user_role           AS ENUM ('ADMIN', 'RECRUITER', 'MANAGER');
CREATE TYPE job_status          AS ENUM ('DRAFT', 'PUBLISHED', 'PAUSED', 'CLOSED');
CREATE TYPE application_status  AS ENUM ('PENDING', 'REVIEWED', 'SHORTLISTED', 'REJECTED', 'HIRED');
CREATE TYPE resume_status       AS ENUM ('UPLOADED', 'PROCESSING', 'EXTRACTED', 'FAILED');
CREATE TYPE plan_type           AS ENUM ('FREE', 'STARTER', 'PRO', 'ENTERPRISE');
CREATE TYPE consent_type        AS ENUM ('CV_PROCESSING', 'DATA_RETENTION', 'AI_ANALYSIS');
CREATE TYPE data_request_type   AS ENUM ('ACCESS', 'ERASURE', 'PORTABILITY');

-- ============================================================
-- COMPANIES
-- ============================================================
CREATE TABLE companies (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(100) UNIQUE NOT NULL,
    logo_url    TEXT,
    website     TEXT,
    industry    VARCHAR(100),
    size_range  VARCHAR(50),
    country     VARCHAR(100),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- USERS
-- ============================================================
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id      UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    email           VARCHAR(255) UNIQUE NOT NULL,
    password_hash   TEXT NOT NULL,
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    role            user_role NOT NULL DEFAULT 'RECRUITER',
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_users_company_id ON users(company_id);
CREATE INDEX idx_users_email      ON users(email);

-- ============================================================
-- PASSWORD RESET TOKENS
-- ============================================================
CREATE TABLE password_reset_tokens (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  TEXT NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    used_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_password_reset_user_id ON password_reset_tokens(user_id);

-- ============================================================
-- REFRESH TOKENS
-- ============================================================
CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  TEXT NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- ============================================================
-- SUBSCRIPTIONS
-- ============================================================
CREATE TABLE subscriptions (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id              UUID NOT NULL UNIQUE REFERENCES companies(id) ON DELETE CASCADE,
    plan_type               plan_type NOT NULL DEFAULT 'FREE',
    cv_quota                INT NOT NULL DEFAULT 50,
    cv_used_current_period  INT NOT NULL DEFAULT 0,
    period_start            DATE NOT NULL DEFAULT CURRENT_DATE,
    period_end              DATE NOT NULL DEFAULT (CURRENT_DATE + INTERVAL '1 month'),
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- SKILLS
-- ============================================================
CREATE TABLE skills (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(100) UNIQUE NOT NULL,
    category    VARCHAR(100),
    embedding   vector(1536),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_skills_name      ON skills(name);
CREATE INDEX idx_skills_embedding ON skills USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- ============================================================
-- JOBS
-- ============================================================
CREATE TABLE jobs (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id              UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    created_by              UUID NOT NULL REFERENCES users(id),
    title                   VARCHAR(255) NOT NULL,
    description             TEXT NOT NULL,
    location                VARCHAR(255),
    remote_policy           VARCHAR(50),
    contract_type           VARCHAR(50),
    experience_years_min    INT,
    experience_years_max    INT,
    salary_min              INT,
    salary_max              INT,
    salary_currency         CHAR(3) DEFAULT 'EUR',
    status                  job_status NOT NULL DEFAULT 'DRAFT',
    ai_analyzed_at          TIMESTAMPTZ,
    ai_analysis_json        JSONB,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_jobs_company_id ON jobs(company_id);
CREATE INDEX idx_jobs_status     ON jobs(status);

-- ============================================================
-- JOB REQUIRED SKILLS
-- ============================================================
CREATE TABLE job_required_skills (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_id          UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    skill_id        UUID NOT NULL REFERENCES skills(id),
    weight          DECIMAL(4,2) NOT NULL DEFAULT 1.0,
    is_mandatory    BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (job_id, skill_id)
);
CREATE INDEX idx_job_required_skills_job_id ON job_required_skills(job_id);

-- ============================================================
-- CANDIDATES
-- ============================================================
CREATE TABLE candidates (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id  UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    email       VARCHAR(255) NOT NULL,
    first_name  VARCHAR(100),
    last_name   VARCHAR(100),
    phone       VARCHAR(50),
    linkedin_url TEXT,
    location    VARCHAR(255),
    is_erased   BOOLEAN NOT NULL DEFAULT FALSE,
    erased_at   TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (company_id, email)
);
CREATE INDEX idx_candidates_company_id ON candidates(company_id);
CREATE INDEX idx_candidates_email      ON candidates(email);

-- ============================================================
-- RESUMES
-- ============================================================
CREATE TABLE resumes (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id          UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    candidate_id        UUID NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,
    uploaded_by         UUID NOT NULL REFERENCES users(id),
    original_filename   VARCHAR(500) NOT NULL,
    minio_object_key    TEXT NOT NULL UNIQUE,
    mime_type           VARCHAR(100),
    file_size_bytes     BIGINT,
    status              resume_status NOT NULL DEFAULT 'UPLOADED',
    extracted_data      JSONB,
    extraction_error    TEXT,
    processed_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_resumes_company_id   ON resumes(company_id);
CREATE INDEX idx_resumes_candidate_id ON resumes(candidate_id);
CREATE INDEX idx_resumes_status       ON resumes(status);

-- ============================================================
-- CANDIDATE SKILLS
-- ============================================================
CREATE TABLE candidate_skills (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    candidate_id        UUID NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,
    resume_id           UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    skill_id            UUID NOT NULL REFERENCES skills(id),
    years_experience    DECIMAL(4,1),
    proficiency_level   VARCHAR(50),
    UNIQUE (resume_id, skill_id)
);
CREATE INDEX idx_candidate_skills_candidate_id ON candidate_skills(candidate_id);
CREATE INDEX idx_candidate_skills_resume_id    ON candidate_skills(resume_id);

-- ============================================================
-- APPLICATIONS
-- ============================================================
CREATE TABLE applications (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id      UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    job_id          UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    candidate_id    UUID NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,
    resume_id       UUID NOT NULL REFERENCES resumes(id),
    status          application_status NOT NULL DEFAULT 'PENDING',
    applied_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (job_id, candidate_id)
);
CREATE INDEX idx_applications_company_id   ON applications(company_id);
CREATE INDEX idx_applications_job_id       ON applications(job_id);
CREATE INDEX idx_applications_candidate_id ON applications(candidate_id);

-- ============================================================
-- AI REPORTS
-- ============================================================
CREATE TABLE ai_reports (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id          UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    application_id      UUID NOT NULL UNIQUE REFERENCES applications(id) ON DELETE CASCADE,
    match_score         DECIMAL(5,2) NOT NULL CHECK (match_score BETWEEN 0 AND 100),
    skill_score         DECIMAL(5,2),
    experience_score    DECIMAL(5,2),
    education_score     DECIMAL(5,2),
    language_score      DECIMAL(5,2),
    strengths           TEXT[],
    weaknesses          TEXT[],
    recommendation      TEXT,
    raw_llm_response    JSONB,
    model_used          VARCHAR(100),
    tokens_input        INT,
    tokens_output       INT,
    generated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_ai_reports_company_id     ON ai_reports(company_id);
CREATE INDEX idx_ai_reports_application_id ON ai_reports(application_id);
CREATE INDEX idx_ai_reports_score          ON ai_reports(match_score DESC);

-- ============================================================
-- AUDIT DECISIONS (immutable log)
-- ============================================================
CREATE TABLE audit_decisions (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id              UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    application_id          UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    ai_report_id            UUID REFERENCES ai_reports(id),
    ai_score                DECIMAL(5,2) NOT NULL,
    human_override          BOOLEAN NOT NULL DEFAULT FALSE,
    final_status            application_status NOT NULL,
    override_justification  TEXT,
    decided_by              UUID NOT NULL REFERENCES users(id),
    decided_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_audit_decisions_company_id     ON audit_decisions(company_id);
CREATE INDEX idx_audit_decisions_application_id ON audit_decisions(application_id);
CREATE INDEX idx_audit_decisions_decided_at     ON audit_decisions(decided_at DESC);

-- Immutability rules for audit log
CREATE RULE no_update_audit_decisions AS ON UPDATE TO audit_decisions DO INSTEAD NOTHING;
CREATE RULE no_delete_audit_decisions AS ON DELETE TO audit_decisions DO INSTEAD NOTHING;

-- ============================================================
-- CONSENT LOGS
-- ============================================================
CREATE TABLE consent_logs (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    candidate_id    UUID NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,
    company_id      UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    consent_type    consent_type NOT NULL,
    granted         BOOLEAN NOT NULL,
    ip_address      INET,
    user_agent      TEXT,
    granted_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_consent_logs_candidate_id ON consent_logs(candidate_id);
CREATE INDEX idx_consent_logs_company_id   ON consent_logs(company_id);

-- ============================================================
-- DATA REQUESTS (GDPR)
-- ============================================================
CREATE TABLE data_requests (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    candidate_id    UUID NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,
    company_id      UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    request_type    data_request_type NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    requested_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMPTZ
);
CREATE INDEX idx_data_requests_candidate_id ON data_requests(candidate_id);
CREATE INDEX idx_data_requests_company_id   ON data_requests(company_id);

-- ============================================================
-- DATA RETENTION POLICIES
-- ============================================================
CREATE TABLE data_retention_policies (
    id                          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id                  UUID NOT NULL UNIQUE REFERENCES companies(id) ON DELETE CASCADE,
    resume_retention_days       INT NOT NULL DEFAULT 365,
    candidate_retention_days    INT NOT NULL DEFAULT 730,
    audit_retention_days        INT NOT NULL DEFAULT 1825,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
