CREATE TABLE model_disagreements (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL,
    champion_score DOUBLE PRECISION NOT NULL,
    challenger_score DOUBLE PRECISION NOT NULL,
    champion_decision VARCHAR(50) NOT NULL,
    challenger_decision VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    champion_inference_time_ms BIGINT,
    challenger_inference_time_ms BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_disagreement_type ON model_disagreements (type);
CREATE INDEX idx_created_at ON model_disagreements (created_at);
