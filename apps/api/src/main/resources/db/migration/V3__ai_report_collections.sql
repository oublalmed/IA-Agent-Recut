-- Support for AiReport strengths and weaknesses as separate tables
-- (ElementCollection mapping)
CREATE TABLE IF NOT EXISTS ai_report_strengths (
    ai_report_id UUID NOT NULL REFERENCES ai_reports(id) ON DELETE CASCADE,
    strength     TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS ai_report_weaknesses (
    ai_report_id UUID NOT NULL REFERENCES ai_reports(id) ON DELETE CASCADE,
    weakness     TEXT NOT NULL
);

CREATE INDEX idx_ai_report_strengths_report_id ON ai_report_strengths(ai_report_id);
CREATE INDEX idx_ai_report_weaknesses_report_id ON ai_report_weaknesses(ai_report_id);
