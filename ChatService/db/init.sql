CREATE TABLE IF NOT EXISTS chat_connection (
    id BIGSERIAL PRIMARY KEY,
    first_user_id BIGINT NOT NULL,
    second_user_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (first_user_id, second_user_id)
);

CREATE INDEX IF NOT EXISTS idx_chat_connection_user ON chat_connection (first_user_id, second_user_id);
CREATE INDEX IF NOT EXISTS idx_chat_connection_update_time ON chat_connection (update_time DESC);

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGSERIAL PRIMARY KEY,
    connection_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_chat_message_connection FOREIGN KEY (connection_id) REFERENCES chat_connection(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_chat_message_connection_time ON chat_message (connection_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_chat_message_user ON chat_message (user_id);
