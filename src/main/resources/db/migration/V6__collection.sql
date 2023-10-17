CREATE TABLE unconfirmed_collection (
id                  UUID PRIMARY KEY,
user_id             UUID NOT NULL REFERENCES users(id),
reward_id           UUID NOT NULL REFERENCES reward(id),
stamps_id_used      UUID[] NOT NULL,
created_at          TIMESTAMPTZ NOT NULL
);

CREATE INDEX ON unconfirmed_collection (user_id);
