CREATE TABLE stamp_config (
id                  UUID PRIMARY KEY,
restaurant_id       UUID NOT NULL REFERENCES restaurant(id),
stamps_to_reward    INT NOT NULL,
rewards             UUID[] NOT NULL,
created_at          TIMESTAMPTZ NOT NULL,
archived_at         TIMESTAMPTZ NULL
);

CREATE INDEX ON stamp_config (restaurant_id);