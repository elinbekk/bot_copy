CREATE TABLE links (
                       id BIGSERIAL PRIMARY KEY,
                       chat_id BIGINT NOT NULL REFERENCES chats(id),
                       url TEXT NOT NULL,
                       type VARCHAR(50) NOT NULL,
                       last_checked TIMESTAMPTZ NOT NULL,
                       tags JSONB,
                       filters JSONB
);
CREATE INDEX idx_links_chat_checked ON links(chat_id, last_checked);
