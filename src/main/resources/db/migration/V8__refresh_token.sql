CREATE TABLE user_refresh_token(
user_id         UUID NOT NULL REFERENCES users(id),
refresh_token   TEXT NOT NULL,
user_agent      TEXT NOT NULL,
created_at      TIMESTAMPTZ NOT NULL,
updated_at      TIMESTAMPTZ NOT NULL,
PRIMARY KEY (user_id, user_agent)
);

CREATE INDEX ON user_refresh_token (user_id, refresh_token);