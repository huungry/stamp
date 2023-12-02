CREATE TABLE confirmed_collection (
id                  UUID PRIMARY KEY,
user_id             UUID NOT NULL REFERENCES users(id),
reward_id           UUID NOT NULL REFERENCES reward(id),
stamps_id_used      UUID[] NOT NULL,
created_at          TIMESTAMPTZ NOT NULL,
confirmed_by        UUID NOT NULL REFERENCES users(id),
confirmed_at        TIMESTAMPTZ NOT NULL
);

CREATE INDEX ON confirmed_collection (user_id);
