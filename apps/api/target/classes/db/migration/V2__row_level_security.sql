-- Enable RLS on sensitive tables
ALTER TABLE resumes      ENABLE ROW LEVEL SECURITY;
ALTER TABLE candidates   ENABLE ROW LEVEL SECURITY;
ALTER TABLE jobs         ENABLE ROW LEVEL SECURITY;
ALTER TABLE applications ENABLE ROW LEVEL SECURITY;
ALTER TABLE ai_reports   ENABLE ROW LEVEL SECURITY;

-- RLS policies using app.current_company_id session variable
CREATE POLICY company_isolation_resumes ON resumes
    USING (company_id = current_setting('app.current_company_id', TRUE)::UUID);

CREATE POLICY company_isolation_candidates ON candidates
    USING (company_id = current_setting('app.current_company_id', TRUE)::UUID);

CREATE POLICY company_isolation_jobs ON jobs
    USING (company_id = current_setting('app.current_company_id', TRUE)::UUID);

CREATE POLICY company_isolation_applications ON applications
    USING (company_id = current_setting('app.current_company_id', TRUE)::UUID);

CREATE POLICY company_isolation_ai_reports ON ai_reports
    USING (company_id = current_setting('app.current_company_id', TRUE)::UUID);

-- Allow the app DB user to bypass RLS when needed (superuser operations)
-- The application sets the session variable before each tenant operation
