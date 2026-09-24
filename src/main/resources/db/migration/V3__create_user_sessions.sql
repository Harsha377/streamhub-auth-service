CREATE TABLE user_sessions(
    id BIGSERIAL PRIMARY KEY ,
    session_id UUID NOT NULL UNIQUE ,
    user_id BIGINT NOT NULL ,
    refresh_token_hash VARCHAR(255) NOT NULL ,
    device_name VARCHAR(100),
    browser VARCHAR(100),
    operating_system VARCHAR(100),
    ip_address VARCHAR(50),
    login_at TIMESTAMP NOT NULL ,
    last_activity_at TIMESTAMP NOT NULL ,
    expires_at TIMESTAMP NOT NULL ,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    logout_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL  DEFAULT  CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_session_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_sessions_user
    ON user_sessions(user_id);

CREATE INDEX idx_user_sessions_session
    ON user_sessions(session_id);

CREATE INDEX idx_user_sessions_expiry
    ON user_sessions(expires_at);

CREATE INDEX idx_user_sessions_revoked
    ON user_sessions(revoked);