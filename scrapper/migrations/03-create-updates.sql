CREATE TABLE updates (
                         id BIGSERIAL PRIMARY KEY,
                         link_id BIGINT NOT NULL REFERENCES links(id),
                         occurred_at TIMESTAMPTZ NOT NULL,
                         payload JSONB NOT NULL,
                         sent BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_updates_sent ON updates(sent);
